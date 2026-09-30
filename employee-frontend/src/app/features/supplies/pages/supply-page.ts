import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal, TemplateRef, viewChild } from '@angular/core';
import { FormField, form, min, required, submit } from '@angular/forms/signals';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { Supply } from '@features/supplies/models/supply';
import { SupplyService } from '@features/supplies/services/supply-service';
import { Table, TableColumn } from '@shared/components/table/table';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [CurrencyPipe, FormField, MatDialogModule, Table],
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
    { id: 'price', header: 'Price', template: this.priceCell() },
    { id: 'quantity', header: 'Quantity', value: (supply) => supply.productQuantity },
    { id: 'status', header: 'Status', template: this.statusCell() },
    ...(this.canManageSupplies
      ? [{ id: 'actions', header: 'Actions', template: this.actionsCell() }]
      : []),
  ]);

  protected readonly supplyForm = form(this.newSupply, (path) => {
    required(path.productName, {
      message: 'Name is required',
    });

    required(path.productDescription, {
      message: 'Description is required',
    });

    required(path.productPrice, {
      message: 'Price is required',
    });

    min(path.productPrice, 0.01, {
      message: 'Price must be greater than 0',
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

  protected readonly canManageSupplies =
    this.auth.hasRole(Roles.admin) || this.auth.hasRole(Roles.inventoryManager);
  constructor() {
    this.loadSupplies();
  }

  private loadSupplies(): void {
    if (!this.inventoryId) {
      this.loading.set(false);
      this.error.set(true);
      return;
    }

    this.loading.set(true);
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

    void submit(this.supplyForm, async () => {
      if (!this.inventoryId) {
        return;
      }

      this.addingSupply.set(true);
      this.addError.set('');

      const editingId = this.editingSupplyId();

      const request$ = editingId
        ? this.supplyService.updateSupply(this.inventoryId, editingId, this.newSupply())
        : this.supplyService.createSupply(this.inventoryId, this.newSupply());

      request$.subscribe({
        next: () => {
          // Clear field interaction state too, so the next add form does not show stale validation errors.
          this.supplyForm().reset({
            productName: '',
            productDescription: '',
            productPrice: 0,
            productQuantity: 1,
            productSalePrice: 0,
          });

          this.editingSupplyId.set(null);
          this.showAddForm.set(false);
          this.addingSupply.set(false);

          this.loadSupplies();
        },
        error: () => {
          this.addError.set(editingId ? 'Unable to update supply.' : 'Unable to add supply.');

          this.addingSupply.set(false);
        },
      });
    });
  }

  protected editSupply(supply: Supply): void {
    this.editingSupplyId.set(supply.productId);

    this.newSupply.set({
      productName: supply.productName,
      productDescription: supply.productDescription,
      productPrice: supply.productPrice,
      productQuantity: supply.productQuantity,
      productSalePrice: supply.productSalePrice,
    });

    this.showAddForm.set(true);
  }
  protected deleteSupply(supply: Supply): void {
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
      if (confirmed !== true) {
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
    this.showAddForm.set(true);
  }
  protected cancelAddForm(): void {
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
  }

  protected formatStatus(status: string): string {
    return status.replaceAll('_', ' ');
  }
}
