import { CurrencyPipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { catchError, debounceTime, distinctUntilChanged, EMPTY, Subject, switchMap } from 'rxjs';

import { isApiError } from '@core/models/api-error';
import { AuthState } from '@core/services/auth-state';
import { ProductAddDialog } from '@features/prod/components/product-add-dialog/product-add-dialog';
import { ProductDetailsDialog } from '@features/prod/components/product-details-dialog/product-details-dialog';
import { ProductThumbnail } from '@features/prod/components/product-thumbnail/product-thumbnail';
import { ProductUpdateDialog } from '@features/prod/components/product-update-dialog/product-update-dialog';
import { Product } from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [
    CurrencyPipe,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ProductThumbnail,
  ],
  selector: 'app-prod',
  styleUrl: './prod.css',
  templateUrl: './prod.html',
})
export class Prod implements OnInit {
  private readonly auth = inject(AuthState);
  private readonly productService = inject(ProductService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly productSearch = new Subject<string>();
  private readonly searchTerm = signal('');

  protected readonly products = signal<Product[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly canManageProducts = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager);
  });

  ngOnInit(): void {
    this.productSearch
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((productName) => {
          this.isLoading.set(true);
          this.errorMessage.set(null);
          return this.productService.getProducts(productName ? { productName } : {}).pipe(
            catchError((error: unknown) => {
              this.isLoading.set(false);
              this.errorMessage.set(isApiError(error) ? error.message : 'Could not load products.');
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((products) => {
        this.products.set(products);
        this.isLoading.set(false);
      });

    this.loadProducts();
  }

  protected searchProducts(productName: string): void {
    const trimmedProductName = productName.trim();
    this.searchTerm.set(trimmedProductName);
    this.productSearch.next(trimmedProductName);
  }

  protected loadProducts(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    const productName = this.searchTerm();
    this.productService.getProducts(productName ? { productName } : {}).subscribe({
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

  protected openProductDetails(productId: string): void {
    this.dialog.open(ProductDetailsDialog, {
      data: { productId },
      width: '850px',
      maxWidth: '95vw',
      maxHeight: '90vh',
    });
  }

  protected openProductUpdate(product: Product): void {
    if (!this.canManageProducts()) {
      return;
    }

    this.dialog
      .open(ProductUpdateDialog, {
        data: { product },
        width: '850px',
        maxWidth: '95vw',
        maxHeight: '90vh',
      })
      .afterClosed()
      .subscribe((updatedProduct?: Product) => {
        if (updatedProduct) {
          this.loadProducts();
        }
      });
  }
}
