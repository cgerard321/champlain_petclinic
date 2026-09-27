import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthState } from '@core/services/auth-state';
import { isApiError } from '@core/models/api-error';
import { Roles } from '@shared/models/roles';
import { Product } from '@features/prod/models/product.model';
import { ProductAddDialog } from '@features/prod/components/product-add-dialog/product-add-dialog';
import { ProductService } from '@features/prod/services/product.service';

@Component({
  imports: [CurrencyPipe, MatButtonModule, MatCardModule, MatDialogModule, MatProgressSpinnerModule],
  selector: 'app-prod',
  styleUrl: './prod.css',
  templateUrl: './prod.html',
})
export class Prod {
  private readonly auth = inject(AuthState);
  private readonly productService = inject(ProductService);
  private readonly dialog = inject(MatDialog);

  protected readonly products = signal<Product[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly canManageProducts = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager);
  });

  ngOnInit(): void {
    this.loadProducts();
  }

  protected loadProducts(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.productService.getProducts().subscribe({
      next: (products) => {
        this.products.set(products);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.errorMessage.set(isApiError(error) ? error.message : 'Could not load products.');
      },
    });
  }

  protected openAddProduct(): void {
    if (!this.canManageProducts()) {
      return;
    }

    this.dialog
      .open(ProductAddDialog, {
        autoFocus: 'first-tabbable',
        width: '850px',
        maxWidth: '95vw',
        maxHeight: '90vh',
      })
      .afterClosed()
      .subscribe((product?: Product) => {
        if (product) {
          this.loadProducts();
        }
      });
  }
}
