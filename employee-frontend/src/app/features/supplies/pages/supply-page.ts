import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal, TemplateRef, viewChild } from '@angular/core';
import { FormField, form, min, required, submit } from '@angular/forms/signals';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { SupplyImageEditor } from '@features/supplies/components/supply-image-editor/supply-image-editor';
import { Supply } from '@features/supplies/models/supply';
import { SupplyService } from '@features/supplies/services/supply-service';
import { Table, TableColumn } from '@shared/components/table/table';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [CurrencyPipe, FormField, MatDialogModule, Table, SupplyImageEditor],
  selector: 'app-supply',
  styleUrl: './supply-page.css',
  templateUrl: './supply-page.html',
})
export class SupplyPage {
  private readonly route = inject(ActivatedRoute);
  private readonly supplyService = inject(SupplyService);
  private readonly auth = inject(AuthState);
  private readonly dialog = inject(MatDialog);
  private readonly deleteDialogTemplate = viewChild.required<TemplateRef<unknown>>('deleteDialog');

  protected readonly inventoryId = this.route.snapshot.paramMap.get('inventoryId');

  protected readonly supplies = signal<Supply[]>([]);
  // Only the initial fetch replaces the table; refreshes keep existing rows visible.
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly addingSupply = signal(false);
  protected readonly addError = signal('');
  protected readonly deleteError = signal('');
  protected readonly showAddForm = signal(false);
  protected readonly editingSupplyId = signal<string | null>(null);
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
    ...(this.canManageSupplies()
      ? [{ id: 'actions', header: 'Actions', template: this.actionsCell() }]
      : []),
  ]);

  protected readonly selectedImage = signal<File | null>(null);
  protected readonly currentImage = signal<string | null>(null);
  protected readonly imageChanged = signal(false);

  protected onImageChange(file: File | null): void {
    this.selectedImage.set(file);
    this.imageChanged.set(true);
  }

  protected readonly supplyForm = form(this.newSupply, (path) => {
    required(path.productName, {
      message: 'Name is required',
    });

    required(path.productDescription, {
      message: 'Description is required',
    });

    required(path.productPrice, {
      message: 'Cost price is required',
    });

    min(path.productPrice, 0.01, {
      message: 'Cost price must be greater than 0',
    });

    required(path.productQuantity, {
      message: 'Quantity is required',
    });

    min(path.productQuantity, 1, {
      message: 'Quantity must be at least 1',
    });

    required(path.productSalePrice, {
      message: 'Sale price is required',
    });

    min(path.productSalePrice, 0.01, {
      message: 'Sale price must be greater than 0',
    });
  });

  protected readonly canManageSupplies = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager);
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

  protected addSupply(event: Event): void {
    event.preventDefault();

    if (this.addingSupply()) return;

    void submit(this.supplyForm, async () => {
      const inventoryId = this.inventoryId;

      if (!inventoryId || !this.canManageSupplies()) return;

      this.addingSupply.set(true);
      this.addError.set('');

      const editingId = this.editingSupplyId();

      try {
        const photo: {
          photoData?: string;
          photoType?: string | null;
        } = {};

        if (this.imageChanged()) {
          const file = this.selectedImage();

          // Empty Base64 decodes to an empty byte array: remove the photo.
          photo.photoData = file ? await this.readPhoto(file) : '';
          photo.photoType = file ? file.type : null;
        }

        const supply = { ...this.newSupply(), ...photo };

        const request$ = editingId
          ? this.supplyService.updateSupply(inventoryId, editingId, supply)
          : this.supplyService.createSupply(inventoryId, supply);

        await firstValueFrom(request$);

        this.cancelAddForm();
        this.loadSupplies();
      } catch {
        this.addError.set(
          editingId ? 'Unable to update supply or photo.' : 'Unable to add supply or photo.',
        );
      } finally {
        this.addingSupply.set(false);
      }
    });
  }

  protected editSupply(supply: Supply): void {
    this.selectedImage.set(null);
    if (!this.canManageSupplies()) {
      return;
    }
    this.selectedImage.set(null);
    this.imageChanged.set(false);
    this.currentImage.set(this.supplyPhotoUrl(supply));

    this.editingSupplyId.set(supply.productId);
    this.addError.set('');

    // reset() clears touched and dirty state from the previous edit as it loads this supply.
    this.supplyForm().reset({
      productName: supply.productName,
      productDescription: supply.productDescription,
      productPrice: supply.productPrice,
      productQuantity: supply.productQuantity,
      productSalePrice: supply.productSalePrice,
    });

    this.showAddForm.set(true);
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

    const dialogRef = this.dialog.open<unknown, Supply, boolean>(this.deleteDialogTemplate(), {
      data: supply,
      ariaLabel: 'Delete supply',
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed !== true || !this.canManageSupplies()) {
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
  protected openAddForm(): void {
    if (!this.canManageSupplies()) {
      return;
    }

    this.selectedImage.set(null);
    this.imageChanged.set(false);
    this.currentImage.set(null);

    this.showAddForm.set(true);
  }
  protected cancelAddForm(): void {
    this.selectedImage.set(null);
    this.supplyForm().reset({
      productName: '',
      productDescription: '',
      productPrice: 0,
      productQuantity: 1,
      productSalePrice: 0,
    });
    this.editingSupplyId.set(null);
    this.addError.set('');
    this.showAddForm.set(false);
    this.selectedImage.set(null);
    this.imageChanged.set(false);
    this.currentImage.set(null);
  }

  protected formatStatus(status: string): string {
    return status.replaceAll('_', ' ');
  }

  protected supplyPhotoUrl(supply: Supply): string | null {
    if (!supply.photoData || !['image/jpeg', 'image/png'].includes(supply.photoType ?? '')) {
      return null;
    }

    return `data:${supply.photoType};base64,${supply.photoData}`;
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
