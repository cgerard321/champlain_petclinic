import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  computed,
  DestroyRef,
  inject,
  LOCALE_ID,
  signal,
  TemplateRef,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom, Subscription } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { Supply, SupplyRequest } from '@features/supplies/models/supply';
import {
  SupplyHistoryDialog,
  SupplyHistoryDialogData,
} from '@features/supplies/pages/supply-history-dialog';
import { SupplyService } from '@features/supplies/services/supply-service';
import { ALLOWED_IMAGE_TYPES, ImageAction } from '@shared/components/image-editor/image-editor';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import {
  ModalFormData,
  ModalFormField,
  ModalFormResult,
} from '@shared/components/modal-form/modal-form-models';
import { Table, TableColumn } from '@shared/components/table/table';
import { Roles } from '@shared/models/roles';

// The shape of the modal's form: the editable Supply fields plus the photo control.
interface SupplyFormValue {
  productNameEn: string;
  productNameFr: string;
  supplierName: string;
  productDescriptionEn: string;
  productDescriptionFr: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  expirationDate: string;
  photo: ImageAction;
}

@Component({
  imports: [CurrencyPipe, MatIconModule, MatMenuModule, Table],
  selector: 'app-supply',
  styleUrl: './supply-page.css',
  templateUrl: './supply-page.html',
})
export class SupplyPage {
  private readonly route = inject(ActivatedRoute);
  private readonly supplyService = inject(SupplyService);
  private readonly destroyRef = inject(DestroyRef);
  private supplyLoadSubscription?: Subscription;
  private readonly auth = inject(AuthState);
  private readonly dialog = inject(MatDialog);
  private readonly locale = inject(LOCALE_ID);

  protected readonly isFrench = this.locale.startsWith('fr');
  protected readonly tableLabel = $localize`:@@supply.table.label:Articles dans cet inventaire`;
  protected readonly downloadLabel = $localize`:@@supply.actions.download:Télécharger PDF`;
  protected readonly downloadingLabel = $localize`:@@supply.actions.downloading:Téléchargement…`;
  protected readonly consumeLabel = $localize`:@@supply.actions.consume:Utiliser l'article`;
  protected readonly cannotConsumeStockLabel = $localize`:@@supply.actions.cannotConsumeStock:Impossible d'utiliser l'article : rupture de stock`;
  protected readonly cannotConsumeExpiredLabel = $localize`:@@supply.actions.cannotConsumeExpired:Impossible d'utiliser l'article : produit périmé`;
  protected readonly consumingLabel = $localize`:@@supply.actions.consuming:Utilisation…`;
  protected readonly consumeButtonLabel = $localize`:@@supply.actions.consumeButton:Utiliser`;

  protected readonly inventoryId = this.route.snapshot.paramMap.get('inventoryId');

  protected readonly supplies = signal<Supply[]>([]);
  protected readonly filters = signal({
    productName: '',
    supplierName: '',
    productDescription: '',
    status: '',
  });
  protected readonly hasActiveFilters = computed(() =>
    Object.values(this.filters()).some((value) => value.trim().length > 0),
  );

  protected readonly filteredSupplies = computed(() => {
    const { productName, supplierName, productDescription, status } = this.filters();
    const nameQuery = productName.trim().toLowerCase();
    const descriptionQuery = productDescription.trim().toLowerCase();
    const supplierQuery = supplierName.trim().toLowerCase();
    return this.supplies().filter(
      (supply) =>
        [supply.name?.en, supply.name?.fr, supply.productName].some((text) =>
          (text ?? '').toLowerCase().includes(nameQuery),
        ) &&
        this.supplierLabel(supply).toLowerCase().includes(supplierQuery) &&
        [supply.description?.en, supply.description?.fr, supply.productDescription].some((text) =>
          (text ?? '').toLowerCase().includes(descriptionQuery),
        ) &&
        (!status || this.effectiveStatus(supply) === status),
    );
  });

  protected openSupplyHistory(supply: Supply): void {
    if (!this.canManageSupplies() || !this.inventoryId) {
      return;
    }

    this.dialog.open<SupplyHistoryDialog, SupplyHistoryDialogData>(SupplyHistoryDialog, {
      data: {
        inventoryId: this.inventoryId,
        productId: supply.productId,
        supplyName: this.localizedSupplyName(supply),
      },
      ariaLabel: $localize`:@@supply.history.ariaLabel:Historique de l'article ${this.localizedSupplyName(supply)}:name:`,
      width: '640px',
      maxWidth: '95vw',
    });
  }

  protected setFilter(
    field: 'productName' | 'supplierName' | 'productDescription' | 'status',
    event: Event,
  ): void {
    const value = (event.target as HTMLInputElement | HTMLSelectElement).value;
    this.filters.update((filters) => ({ ...filters, [field]: value }));
  }

  protected clearFilters(): void {
    this.filters.set({ productName: '', supplierName: '', productDescription: '', status: '' });
  }

  // Only the initial fetch replaces the table; refreshes keep existing rows visible.
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly addingSupply = signal(false);
  protected readonly addError = signal('');
  protected readonly deleteError = signal('');
  protected readonly downloadingPdf = signal(false);
  protected readonly downloadError = signal('');
  protected readonly consumingSupplyId = signal<string | null>(null);

  protected readonly nameCell = viewChild<TemplateRef<{ $implicit: Supply }>>('nameCell');
  protected readonly priceCell = viewChild<TemplateRef<{ $implicit: Supply }>>('priceCell');
  protected readonly statusCell = viewChild<TemplateRef<{ $implicit: Supply }>>('statusCell');
  protected readonly actionsCell = viewChild<TemplateRef<{ $implicit: Supply }>>('actionsCell');
  protected readonly expirationCell =
    viewChild<TemplateRef<{ $implicit: Supply }>>('expirationCell');
  protected readonly supplyRowId = (supply: Supply): string => supply.productId;

  // Formatting and actions stay here so the shared table has no supply-specific logic.
  protected readonly columns = computed<TableColumn<Supply>[]>(() => [
    { id: 'name', header: $localize`:@@supply.fields.name:Nom`, template: this.nameCell() },
    {
      id: 'supplier',
      header: $localize`:@@supply.fields.supplier:Fournisseur`,
      value: (supply) => this.supplierLabel(supply),
    },
    {
      id: 'description',
      header: $localize`:@@supply.fields.description:Description`,
      value: (supply) => this.localizedSupplyDescription(supply),
    },
    {
      id: 'price',
      header: $localize`:@@supply.fields.costPrice:Prix coûtant (CAD)`,
      template: this.priceCell(),
    },
    {
      id: 'quantity',
      header: $localize`:@@supply.fields.quantity:Quantité`,
      value: (supply) => supply.productQuantity,
    },
    {
      id: 'expiration',
      header: $localize`:@@supply.fields.expiration:Date d'expiration`,
      template: this.expirationCell(),
    },
    {
      id: 'status',
      header: $localize`:@@supply.fields.status:État`,
      template: this.statusCell(),
    },
    ...(this.canManageSupplies() || this.canConsumeSupplies()
      ? [
          {
            id: 'actions',
            header: $localize`:@@supply.fields.actions:Actions`,
            template: this.actionsCell(),
          },
        ]
      : []),
  ]);

  protected localizedSupplyName(supply: Supply): string {
    return (this.isFrench ? supply.name?.fr : supply.name?.en) ?? supply.productName;
  }

  protected localizedSupplyDescription(supply: Supply): string {
    return (
      (this.isFrench ? supply.description?.fr : supply.description?.en) ?? supply.productDescription
    );
  }

  private supplierLabel(supply: Supply): string {
    return supply.supplierName?.trim() || $localize`:@@supply.supplier.notRecorded:Non indiqué`;
  }

  protected readonly canManageSupplies = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager);
  });

  protected readonly canConsumeSupplies = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.vet);
  });

  constructor() {
    this.loadSupplies();
  }

  private loadSupplies(): void {
    this.supplyLoadSubscription?.unsubscribe();
    this.supplies.set([]);
    this.loading.set(true);
    this.error.set(false);

    if (!this.inventoryId) {
      this.loading.set(false);
      this.error.set(true);
      return;
    }

    this.supplyLoadSubscription = this.supplyService
      .getSupplies(this.inventoryId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (supply) => {
          this.supplies.update((current) => {
            const index = current.findIndex((item) => item.productId === supply.productId);
            if (index === -1) return [...current, supply];

            const updated = [...current];
            updated[index] = supply;
            return updated;
          });
          this.loading.set(false);
        },
        error: () => {
          this.error.set(true);
          this.loading.set(false);
        },
        complete: () => this.loading.set(false),
      });
  }

  protected openAddDialog(): void {
    if (!this.canManageSupplies()) {
      return;
    }

    void this.openSupplyDialog();
  }

  protected editSupply(supply: Supply): void {
    if (!this.canManageSupplies()) {
      return;
    }

    void this.openSupplyDialog(supply);
  }

  private supplyFields(supply?: Supply): readonly ModalFormField[] {
    const currentImageUrl = supply ? this.supplyPhotoUrl(supply) : null;

    return [
      {
        key: 'productNameEn',
        label: $localize`:@@supply.fields.nameEn:Nom (anglais)`,
        type: 'string',
        required: true,
      },
      {
        key: 'productNameFr',
        label: $localize`:@@supply.fields.nameFr:Nom (français)`,
        type: 'string',
        required: true,
      },
      {
        key: 'supplierName',
        label: $localize`:@@supply.fields.supplierName:Nom du fournisseur`,
        type: 'string',
        required: !supply || Boolean(supply.supplierName?.trim()),
        validators: [
          (control) =>
            typeof control.value === 'string' &&
            control.value.length > 0 &&
            control.value.trim().length === 0
              ? { whitespace: true }
              : null,
        ],
        errorMessages: {
          whitespace: $localize`:@@supply.validation.supplierWhitespace:Le nom du fournisseur ne peut pas contenir seulement des espaces.`,
        },
      },
      {
        key: 'productDescriptionEn',
        label: $localize`:@@supply.fields.descriptionEn:Description (anglais)`,
        type: 'string',
        required: true,
      },
      {
        key: 'productDescriptionFr',
        label: $localize`:@@supply.fields.descriptionFr:Description (français)`,
        type: 'string',
        required: true,
      },
      {
        key: 'productPrice',
        label: $localize`:@@supply.fields.costPrice:Prix coûtant (CAD)`,
        type: 'number',
        required: true,
        min: 0.01,
        step: 0.01,
      },
      {
        key: 'productQuantity',
        label: $localize`:@@supply.fields.quantity:Quantité`,
        type: 'number',
        required: true,
        min: 1,
        step: 1,
      },
      {
        key: 'productSalePrice',
        label: $localize`:@@supply.fields.salePrice:Prix de vente (CAD)`,
        type: 'number',
        required: true,
        min: 0.01,
        step: 0.01,
      },
      {
        key: 'expirationDate',
        label: $localize`:@@supply.fields.expiration:Date d'expiration`,
        type: 'date',
        // Only reject past expiry dates when creating new supplies
        minDate: supply ? undefined : this.todayAsText(),
        errorMessages: {
          minDate: $localize`:@@supply.validation.expirationPast:La date d'expiration ne peut pas être dans le passé.`,
        },
      },
      {
        key: 'photo',
        label: $localize`:@@supply.fields.photo:Photo de l'article`,
        type: 'image',
        currentImageUrl,
      },
    ];
  }

  private async openSupplyDialog(supply?: Supply): Promise<void> {
    this.addError.set('');

    const dialogRef = this.dialog.open<
      ModalForm<SupplyFormValue>,
      ModalFormData<SupplyFormValue>,
      ModalFormResult<SupplyFormValue>
    >(ModalForm, {
      data: {
        title: supply
          ? $localize`:@@supply.actions.editTitle:Modifier l'article`
          : $localize`:@@supply.actions.add:Ajouter un article`,
        submitLabel: supply
          ? $localize`:@@supply.actions.saveChanges:Enregistrer les modifications`
          : $localize`:@@supply.actions.add:Ajouter un article`,
        cancelLabel: $localize`:@@supply.actions.cancel:Annuler`,
        fields: this.supplyFields(supply),
        initialValue: supply
          ? {
              productNameEn: supply.name?.en ?? supply.productName,
              productNameFr: supply.name?.fr ?? supply.productName,
              supplierName: supply.supplierName ?? '',
              productDescriptionEn: supply.description?.en ?? supply.productDescription,
              productDescriptionFr: supply.description?.fr ?? supply.productDescription,
              productPrice: supply.productPrice,
              productQuantity: supply.productQuantity,
              productSalePrice: supply.productSalePrice,
              expirationDate: supply.expirationDate ?? '',
            }
          : {
              productNameEn: '',
              productNameFr: '',
              supplierName: '',
              productDescriptionEn: '',
              productDescriptionFr: '',
              productPrice: 0,
              productQuantity: 1,
              productSalePrice: 0,
              expirationDate: '',
            },
      },
      ariaLabel: supply
        ? $localize`:@@supply.actions.editTitle:Modifier l'article`
        : $localize`:@@supply.actions.add:Ajouter un article`,
      width: '560px',
      maxWidth: '95vw',
    });

    // The dialog closes with undefined when the user cancels.
    const result = await firstValueFrom(dialogRef.afterClosed());

    if (!result || !this.canManageSupplies()) {
      return;
    }

    await this.saveSupply(result.value, supply ?? null);
  }

  private async saveSupply(value: SupplyFormValue, original: Supply | null): Promise<void> {
    const inventoryId = this.inventoryId;
    const editingId = original?.productId ?? null;

    if (!inventoryId || this.addingSupply()) {
      return;
    }

    this.addingSupply.set(true);
    this.addError.set('');

    try {
      const payload = await this.buildSupplyRequest(value);

      const request$ = editingId
        ? this.supplyService.updateSupply(inventoryId, editingId, payload)
        : this.supplyService.createSupply(inventoryId, payload);

      await firstValueFrom(request$);

      this.loadSupplies();
    } catch {
      this.addError.set(
        editingId
          ? $localize`:@@supply.errors.update:Impossible de modifier l'article ou la photo.`
          : $localize`:@@supply.errors.create:Impossible d'ajouter l'article ou la photo.`,
      );
    } finally {
      this.addingSupply.set(false);
    }
  }

  protected consumeSupply(supply: Supply): void {
    if (!this.canConsumeSupplies() || !this.inventoryId) {
      return;
    }

    // Prevent consuming if quantity is already 0 or if the supply is expired
    if (supply.productQuantity <= 0 || this.expirationState(supply) === 'expired') {
      return;
    }
    this.deleteError.set('');

    this.consumingSupplyId.set(supply.productId);

    this.supplyService.consumeSupply(this.inventoryId, supply.productId).subscribe({
      next: () => {
        this.consumingSupplyId.set(null);
        this.loadSupplies();
      },
      error: () => {
        this.consumingSupplyId.set(null);
        this.deleteError.set($localize`:@@supply.errors.consume:Impossible d'utiliser l'article.`);
      },
    });
  }

  protected deleteSupply(supply: Supply): void {
    if (!this.canManageSupplies()) {
      return;
    }

    this.deleteError.set('');

    const inventoryId = this.inventoryId;

    if (!inventoryId) {
      this.deleteError.set(
        $localize`:@@supply.errors.missingInventory:Impossible de supprimer l'article : identifiant d'inventaire manquant.`,
      );
      return;
    }

    const dialogRef = this.dialog.open<
      ModalForm<Record<string, never>>,
      ModalFormData<Record<string, never>>,
      ModalFormResult<Record<string, never>>
    >(ModalForm, {
      data: {
        title: $localize`:@@supply.actions.deleteTitle:Supprimer l'article`,
        message: $localize`:@@supply.actions.deleteConfirm:Voulez-vous vraiment supprimer ${this.localizedSupplyName(supply)}:name: ?`,
        fields: [],
        initialValue: {},
        submitLabel: $localize`:@@supply.actions.delete:Supprimer`,
        cancelLabel: $localize`:@@supply.actions.cancel:Annuler`,
        destructive: true,
      },
      ariaLabel: $localize`:@@supply.actions.deleteTitle:Supprimer l'article`,
      width: '440px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result || !this.canManageSupplies()) {
        return;
      }

      this.supplyService.deleteSupply(inventoryId, supply.productId).subscribe({
        next: () => {
          this.loadSupplies();
        },
        error: () => {
          this.deleteError.set(
            $localize`:@@supply.errors.delete:Impossible de supprimer l'article.`,
          );
        },
      });
    });
  }

  protected downloadPdf(): void {
    const inventoryId = this.inventoryId;

    if (!inventoryId || this.downloadingPdf() || !this.canManageSupplies()) {
      return;
    }

    this.downloadingPdf.set(true);
    this.downloadError.set('');

    this.supplyService
      .downloadSupplyPdf(inventoryId, this.isFrench ? 'fr' : 'en')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          this.saveFile(blob, `supply-report-${inventoryId}.pdf`);
          this.downloadingPdf.set(false);
        },
        error: async (err: unknown) => {
          this.downloadError.set(await this.readDownloadError(err));
          this.downloadingPdf.set(false);
        },
      });
  }

  private saveFile(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url));
  }

  private async readDownloadError(err: unknown): Promise<string> {
    // With responseType 'blob', error bodies also arrive as Blobs, so read the message from it
    if (err instanceof HttpErrorResponse && err.error instanceof Blob) {
      try {
        const body = JSON.parse(await err.error.text());
        if (typeof body?.message === 'string' && !this.isFrench) {
          return body.message;
        }
      } catch {
        // Not JSON, fall back to the generic message
      }
    }
    return $localize`:@@supply.errors.download:Impossible de télécharger le PDF.`;
  }

  // ------ Helpers ------

  // Supplies expiring within this many days are flagged as "expiring soon".
  private readonly expiringSoonDays = 30;

  protected effectiveStatus(supply: Supply): string {
    if (this.expirationState(supply) === 'expired') {
      return 'OUT_OF_STOCK';
    }
    return supply.status;
  }

  protected expirationState(supply: Supply): 'none' | 'expired' | 'soon' | 'ok' {
    if (!supply.expirationDate) {
      return 'none';
    }

    // Adding T00:00:00 makes the date local. Without it, "2026-10-20" is read as UTC,
    // which is the evening of the 19th in Montreal and would be off by one day.
    const expiration = new Date(`${supply.expirationDate}T00:00:00`);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const daysLeft = Math.round((expiration.getTime() - today.getTime()) / 86_400_000);

    if (daysLeft < 0) {
      return 'expired';
    }

    return daysLeft <= this.expiringSoonDays ? 'soon' : 'ok';
  }

  private todayAsText(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');

    return `${now.getFullYear()}-${month}-${day}`;
  }

  protected formatStatus(status: string): string {
    switch (status) {
      case 'AVAILABLE':
        return $localize`:@@supply.status.available:Disponible`;
      case 'RE_ORDER':
        return $localize`:@@supply.status.reOrder:À commander`;
      case 'OUT_OF_STOCK':
        return $localize`:@@supply.status.outOfStock:Rupture de stock`;
      default:
        return status.replaceAll('_', ' ');
    }
  }

  protected supplyPhotoUrl(supply: Supply): string | null {
    const type = supply.photoType ?? '';

    if (!supply.photoData || !(ALLOWED_IMAGE_TYPES as readonly string[]).includes(type)) {
      return null;
    }

    return `data:${type};base64,${supply.photoData}`;
  }

  private readPhoto(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = () => {
        const result = reader.result;

        if (typeof result !== 'string') {
          reject(new Error('Unable to read photo.'));
          return;
        }

        resolve(result.substring(result.indexOf(',') + 1));
      };

      reader.onerror = () => reject(new Error('Unable to read photo.'));
      reader.onabort = () => reject(new Error('Photo reading cancelled.'));
      reader.readAsDataURL(file);
    });
  }

  private async buildSupplyRequest(value: SupplyFormValue): Promise<SupplyRequest> {
    const { photo, expirationDate, supplierName, ...fields } = value;
    const normalizedSupplierName = supplierName.trim();
    const request: SupplyRequest = {
      productName: fields.productNameEn.trim(),
      productDescription: fields.productDescriptionEn.trim(),
      productPrice: fields.productPrice,
      productQuantity: fields.productQuantity,
      productSalePrice: fields.productSalePrice,
      name: { en: fields.productNameEn.trim(), fr: fields.productNameFr.trim() },
      description: {
        en: fields.productDescriptionEn.trim(),
        fr: fields.productDescriptionFr.trim(),
      },
      expirationDate: expirationDate || null,
      ...(normalizedSupplierName ? { supplierName: normalizedSupplierName } : {}),
    };

    switch (photo.action) {
      case 'replace':
        request.photoData = await this.readPhoto(photo.file);
        request.photoType = photo.file.type;
        break;
      case 'remove':
        request.photoData = '';
        request.photoType = null;
        break;
      case 'keep':
        break; // Nothing changes in the backend
    }

    return request;
  }
}
