import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal, TemplateRef, viewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { Supply, SupplyRequest } from '@features/supplies/models/supply';
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
  productName: string;
  supplierName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  expirationDate: string;
  photo: ImageAction;
}

@Component({
  imports: [CurrencyPipe, MatIconModule, Table],
  selector: 'app-supply',
  styleUrl: './supply-page.css',
  templateUrl: './supply-page.html',
})
export class SupplyPage {
  private readonly route = inject(ActivatedRoute);
  private readonly supplyService = inject(SupplyService);
  private readonly auth = inject(AuthState);
  private readonly dialog = inject(MatDialog);

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
    return this.supplies().filter(
      (supply) =>
        supply.productName.toLowerCase().includes(productName.trim().toLowerCase()) &&
        this.supplierLabel(supply).toLowerCase().includes(supplierName.trim().toLowerCase()) &&
        supply.productDescription.toLowerCase().includes(productDescription.trim().toLowerCase()) &&
        (!status || this.effectiveStatus(supply) === status),
    );
  });

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
    { id: 'name', header: 'Name', template: this.nameCell() },
    {
      id: 'supplier',
      header: 'Supplier',
      value: (supply) => this.supplierLabel(supply),
    },
    { id: 'description', header: 'Description', value: (supply) => supply.productDescription },
    { id: 'price', header: 'Cost Price', template: this.priceCell() },
    { id: 'quantity', header: 'Quantity', value: (supply) => supply.productQuantity },
    { id: 'expiration', header: 'Expiration Date', template: this.expirationCell() },
    { id: 'status', header: 'Status', template: this.statusCell() },
    ...(this.canManageSupplies() || this.canConsumeSupplies()
      ? [{ id: 'actions', header: 'Actions', template: this.actionsCell() }]
      : []),
  ]);

  private supplierLabel(supply: Supply): string {
    return supply.supplierName?.trim() || 'Not recorded';
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
    if (!this.inventoryId) {
      this.loading.set(false);
      this.error.set(true);
      return;
    }

    this.error.set(false);

    this.supplyService.getSupplies(this.inventoryId).subscribe({
      next: (supplies) => {
        this.supplies.set(supplies);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
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
      { key: 'productName', label: 'Name', type: 'string', required: true },
      {
        key: 'supplierName',
        label: 'Supplier name',
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
        errorMessages: { whitespace: 'Supplier name cannot be only whitespace.' },
      },
      { key: 'productDescription', label: 'Description', type: 'string', required: true },
      {
        key: 'productPrice',
        label: 'Cost Price',
        type: 'number',
        required: true,
        min: 0.01,
        step: 0.01,
      },
      {
        key: 'productQuantity',
        label: 'Quantity',
        type: 'number',
        required: true,
        min: 1,
        step: 1,
      },
      {
        key: 'productSalePrice',
        label: 'Sale Price',
        type: 'number',
        required: true,
        min: 0.01,
        step: 0.01,
      },
      {
        key: 'expirationDate',
        label: 'Expiration Date',
        type: 'date',
        // Only reject past expiry dates when creating new supplies
        minDate: supply ? undefined : this.todayAsText(),
        errorMessages: { minDate: 'Expiration date cannot be in the past.' },
      },
      { key: 'photo', label: 'Supply photo', type: 'image', currentImageUrl },
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
        title: supply ? 'Edit Supply' : 'Add Supply',
        submitLabel: supply ? 'Save Changes' : 'Add Supply',
        fields: this.supplyFields(supply),
        initialValue: supply
          ? {
              productName: supply.productName,
              supplierName: supply.supplierName ?? '',
              productDescription: supply.productDescription,
              productPrice: supply.productPrice,
              productQuantity: supply.productQuantity,
              productSalePrice: supply.productSalePrice,
              expirationDate: supply.expirationDate ?? '',
            }
          : {
              productName: '',
              supplierName: '',
              productDescription: '',
              productPrice: 0,
              productQuantity: 1,
              productSalePrice: 0,
              expirationDate: '',
            },
      },
      ariaLabel: supply ? 'Edit supply' : 'Add supply',
      width: '560px',
      maxWidth: '95vw',
    });

    // The dialog closes with undefined when the user cancels.
    const result = await firstValueFrom(dialogRef.afterClosed());

    if (!result || !this.canManageSupplies()) {
      return;
    }

    await this.saveSupply(result.value, supply?.productId ?? null);
  }

  private async saveSupply(value: SupplyFormValue, editingId: string | null): Promise<void> {
    const inventoryId = this.inventoryId;

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
        editingId ? 'Unable to update supply or photo.' : 'Unable to add supply or photo.',
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
        this.deleteError.set('Unable to consume supply.');
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
      this.deleteError.set('Unable to delete supply: the inventory ID is missing.');
      return;
    }

    const dialogRef = this.dialog.open<
      ModalForm<Record<string, never>>,
      ModalFormData<Record<string, never>>,
      ModalFormResult<Record<string, never>>
    >(ModalForm, {
      data: {
        title: 'Delete Supply',
        message: `Are you sure you want to delete ${supply.productName}?`,
        fields: [],
        initialValue: {},
        submitLabel: 'Delete',
        destructive: true,
      },
      ariaLabel: 'Delete supply',
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
          this.deleteError.set('Unable to delete supply.');
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

    this.supplyService.downloadSupplyPdf(inventoryId).subscribe({
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
        if (typeof body?.message === 'string') {
          return body.message;
        }
      } catch {
        // Not JSON, fall back to the generic message
      }
    }
    return 'Unable to download PDF.';
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
    return status.replaceAll('_', ' ');
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
      ...fields,
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
