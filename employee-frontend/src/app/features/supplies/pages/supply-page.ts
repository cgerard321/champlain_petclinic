import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormField, form, min, required, submit } from '@angular/forms/signals';
import { ActivatedRoute } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { Supply } from '@features/supplies/models/supply';
import { SupplyService } from '@features/supplies/services/supply-service';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [CurrencyPipe, FormField],
  selector: 'app-supply',
  styleUrl: './supply-page.css',
  templateUrl: './supply-page.html',
})
export class SupplyPage {
  private readonly route = inject(ActivatedRoute);
  private readonly supplyService = inject(SupplyService);
  private readonly auth = inject(AuthState);

  protected readonly inventoryId = this.route.snapshot.paramMap.get('inventoryId');

  protected readonly supplies = signal<Supply[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly addingSupply = signal(false);
  protected readonly addError = signal('');
  protected readonly showAddForm = signal(false);
  protected readonly editingSupplyId = signal<string | null>(null);
  protected readonly newSupply = signal({
    productName: '',
    productDescription: '',
    productPrice: 0,
    productQuantity: 1,
    productSalePrice: 0,
  });

  protected readonly supplyForm = form(this.newSupply, (path) => {
    required(path.productName, {
      message: 'Name is required',
    });

    required(path.productDescription, {
      message: 'Description is required',
    });

    min(path.productPrice, 0.01, {
      message: 'Price must be greater than 0',
    });

    min(path.productQuantity, 1, {
      message: 'Quantity must be at least 1',
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
          this.newSupply.set({
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
    if (!this.inventoryId) {
      return;
    }

    const confirmed = window.confirm(`Delete ${supply.productName}?`);

    if (!confirmed) {
      return;
    }

    this.supplyService.deleteSupply(this.inventoryId, supply.productId).subscribe({
      next: () => {
        this.loadSupplies();
      },
      error: () => {
        this.addError.set('Unable to delete supply.');
      },
    });
  }
  protected toggleAddForm(): void {
    this.showAddForm.update((visible) => !visible);
  }
  protected formatStatus(status: string): string {
    return status.replaceAll('_', ' ');
  }
}
