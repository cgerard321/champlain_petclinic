import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal, TemplateRef, viewChild } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { Supply, SupplyRequest } from '@features/supplies/models/supply';
import { SupplyService } from '@features/supplies/services/supply-service';
import { ALLOWED_IMAGE_TYPES } from '@shared/components/image-editor/image-editor';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import {
  ModalFormData,
  ModalFormField,
  ModalFormImageValue,
  ModalFormResult,
} from '@shared/components/modal-form/modal-form-models';
import { Table, TableColumn } from '@shared/components/table/table';
import { Roles } from '@shared/models/roles';

// The shape of the modal's form: the editable Supply fields plus the photo control.
interface SupplyFormValue {
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  photo: ModalFormImageValue;
}

@Component({
  imports: [CurrencyPipe, MatDialogModule, MatIconModule, Table],
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
  protected readonly filters = signal({ productName: '', productDescription: '', status: '' });
  protected readonly hasActiveFilters = computed(() =>
    Object.values(this.filters()).some((value) => value.trim().length > 0),
  );
  protected readonly filteredSupplies = computed(() => {
    const { productName, productDescription, status } = this.filters();
    return this.supplies().filter(
      (supply) =>
        supply.productName.toLowerCase().includes(productName.trim().toLowerCase()) &&
        supply.productDescription.toLowerCase().includes(productDescription.trim().toLowerCase()) &&
        (!status || supply.status === status),
    );
  });

  protected setFilter(field: 'productName' | 'productDescription' | 'status', event: Event): void {
    const value = (event.target as HTMLInputElement | HTMLSelectElement).value;
    this.filters.update((filters) => ({ ...filters, [field]: value }));
  }

  protected clearFilters(): void {
    this.filters.set({ productName: '', productDescription: '', status: '' });
  }

  // Only the initial fetch replaces the table; refreshes keep existing rows visible.
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly addingSupply = signal(false);
  protected readonly addError = signal('');
  protected readonly deleteError = signal('');
  protected readonly downloadingPdf = signal(false);
  protected readonly downloadError = signal('');
  protected readonly showAddForm = signal(false);
  protected readonly editingSupplyId = signal<string | null>(null);
  protected readonly consumingSupplyId = signal<string | null>(null);
  protected readonly newSupply = signal({
    productName: '',
    productDescription: '',
    productPrice: 0,
    productQuantity: 1,
    productSalePrice: 0,
  });

  protected readonly nameCell = viewChild<TemplateRef<{ $implicit: Supply }>>('nameCell');
  protected readonly priceCell = viewChild<TemplateRef<{ $implicit: Supply }>>('priceCell');
  protected readonly statusCell = viewChild<TemplateRef<{ $implicit: Supply }>>('statusCell');
  protected readonly actionsCell = viewChild<TemplateRef<{ $implicit: Supply }>>('actionsCell');

  protected readonly supplyRowId = (supply: Supply): string => supply.productId;

  // Formatting and actions stay here so the shared table has no supply-specific logic.
  protected readonly columns = computed<TableColumn<Supply>[]>(() => [
    { id: 'name', header: 'Name', template: this.nameCell() },
    { id: 'description', header: 'Description', value: (supply) => supply.productDescription },
    { id: 'price', header: 'Cost Price', template: this.priceCell() },
    { id: 'quantity', header: 'Quantity', value: (supply) => supply.productQuantity },
    { id: 'status', header: 'Status', template: this.statusCell() },
    ...(this.canManageSupplies() || this.canConsumeSupplies()
      ? [{ id: 'actions', header: 'Actions', template: this.actionsCell() }]
      : []),
  ]);

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

  private supplyFields(currentImageUrl: string | null): readonly ModalFormField[] {
    return [
      { key: 'productName', label: 'Name', type: 'string', required: true },
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
      { key: 'photo', label: 'Supply photo', type: 'image', currentImageUrl },
    ];
  }

  // Opens the dialog for a new supply (no argument) or for editing an existing one.
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
        fields: this.supplyFields(supply ? this.supplyPhotoUrl(supply) : null),
        initialValue: supply
          ? {
              productName: supply.productName,
              productDescription: supply.productDescription,
              productPrice: supply.productPrice,
              productQuantity: supply.productQuantity,
              productSalePrice: supply.productSalePrice,
            }
          : {
              productName: '',
              productDescription: '',
              productPrice: 0,
              productQuantity: 1,
              productSalePrice: 0,
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
      const { photo, ...fields } = value;
      const payload: SupplyRequest = { ...fields };

      // Photo fields are only sent when the user touched the picture.
      // Empty Base64 decodes to an empty byte array: remove the photo.
      if (photo.changed) {
        payload.photoData = photo.file ? await this.readPhoto(photo.file) : '';
        payload.photoType = photo.file ? photo.file.type : null;
      }

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

    if (supply.productQuantity <= 0) {
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
      // result is undefined when the user cancels
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
}
