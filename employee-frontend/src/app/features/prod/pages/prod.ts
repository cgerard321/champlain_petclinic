import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, LOCALE_ID, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTab, MatTabChangeEvent, MatTabGroup } from '@angular/material/tabs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  EMPTY,
  map,
  merge,
  Subject,
  switchMap,
} from 'rxjs';

import { isApiError } from '@core/models/api-error';
import { AuthState } from '@core/services/auth-state';
import { ProductAddDialog } from '@features/prod/components/product-add-dialog/product-add-dialog';
import { ProductBundleList } from '@features/prod/components/product-bundle-list/product-bundle-list';
import { ProductDeleteDialog } from '@features/prod/components/product-delete-dialog/product-delete-dialog';
import { ProductDetailsDialog } from '@features/prod/components/product-details-dialog/product-details-dialog';
import { ProductThumbnail } from '@features/prod/components/product-thumbnail/product-thumbnail';
import { ProductUpdateDialog } from '@features/prod/components/product-update-dialog/product-update-dialog';
import {
  localizedDeliveryType,
  localizedProductDescription,
  localizedProductName,
  localizedProductStatus,
  localizedProductType,
} from '@features/prod/models/product-localization';
import { Product } from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';
import { ReviewSettingsService } from '@features/prod/services/review-settings.service';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [
    CurrencyPipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ProductThumbnail,
    MatTabGroup,
    MatTab,
    ProductBundleList,
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
  private readonly reload = new Subject<void>();
  private readonly searchTerm = signal('');
  private readonly reviewSettingsService = inject(ReviewSettingsService);
  private readonly locale = inject(LOCALE_ID);

  protected readonly isAdmin = computed(() => this.auth.roles().includes(Roles.admin));

  protected readonly products = signal<Product[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly deleteErrorMessage = signal<string | null>(null);
  protected readonly deletingProductId = signal<string | null>(null);
  protected readonly reviewEditPeriodHours = signal(48);
  protected readonly settingsMessage = signal<string | null>(null);
  protected readonly settingsError = signal<string | null>(null);
  protected readonly isSavingSettings = signal(false);
  protected readonly canManageProducts = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager);
  });
  protected readonly selectedTabIndex = signal<number>(
    Number(sessionStorage.getItem('productSelectedTabIndex') ?? 0),
  );

  ngOnInit(): void {
    merge(
      this.productSearch.pipe(debounceTime(300), distinctUntilChanged()),
      this.reload.pipe(map(() => this.searchTerm())),
    )
      .pipe(
        switchMap((productName) => {
          this.isLoading.set(true);
          this.errorMessage.set(null);
          return this.productService.getProducts(productName ? { productName } : {}).pipe(
            catchError((error: unknown) => {
              this.isLoading.set(false);
              this.errorMessage.set(
                isApiError(error)
                  ? error.message
                  : $localize`:@@products.errors.load:Impossible de charger les produits.`,
              );
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

    if (this.isAdmin()) {
      this.loadReviewSettings();
    }
  }

  protected setReviewEditPeriodHours(value: number | string | null): void {
    const parsedValue = value === null || value === '' ? NaN : Number(value);
    this.reviewEditPeriodHours.set(parsedValue);
  }

  protected loadReviewSettings(): void {
    this.reviewSettingsService.getSettings().subscribe({
      next: (settings) => {
        this.reviewEditPeriodHours.set(settings.editPeriodHours);
      },
      error: () => {
        this.settingsError.set(
          $localize`:@@products.reviewSettings.errors.load:Impossible de charger les paramètres des avis.`,
        );
      },
    });
  }

  protected saveReviewSettings(): void {
    const hours = Number(this.reviewEditPeriodHours());

    if (!Number.isInteger(hours) || hours < 0) {
      this.settingsError.set(
        $localize`:@@products.reviewSettings.errors.minimum:La période de modification doit être supérieure ou égale à zéro.`,
      );
      return;
    }
    this.isSavingSettings.set(true);
    this.settingsMessage.set(null);
    this.settingsError.set(null);

    this.reviewSettingsService.updateSettings({ editPeriodHours: hours }).subscribe({
      next: (settings) => {
        this.reviewEditPeriodHours.set(settings.editPeriodHours);
        this.settingsMessage.set(
          $localize`:@@products.reviewSettings.saved:Période de modification des avis enregistrée.`,
        );
        this.isSavingSettings.set(false);
      },
      error: () => {
        this.settingsError.set(
          $localize`:@@products.reviewSettings.errors.save:Impossible d'enregistrer les paramètres des avis.`,
        );
        this.isSavingSettings.set(false);
      },
    });
  }

  protected searchProducts(productName: string): void {
    const trimmedProductName = productName.trim();
    this.searchTerm.set(trimmedProductName);
    this.productSearch.next(trimmedProductName);
  }

  protected loadProducts(): void {
    this.reload.next();
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

  protected openProductDelete(product: Product): void {
    if (!this.canManageProducts() || this.deletingProductId()) {
      return;
    }

    this.deleteErrorMessage.set(null);
    this.dialog
      .open(ProductDeleteDialog, {
        data: {
          title: $localize`:@@products.delete.title:Supprimer le produit?`,
          message: $localize`:@@products.delete.confirm:Voulez-vous vraiment supprimer ${this.productName(product)}:name: ? Cette action est irréversible.`,
          confirmLabel: $localize`:@@products.actions.delete:Supprimer`,
        },
        width: '520px',
        maxWidth: '95vw',
      })
      .afterClosed()
      .subscribe((confirmed?: boolean) => {
        if (confirmed) {
          this.deleteProduct(product, false);
        }
      });
  }

  private deleteProduct(product: Product, cascadeBundles: boolean): void {
    this.deletingProductId.set(product.productId);
    this.deleteErrorMessage.set(null);
    this.productService.deleteProduct(product.productId, cascadeBundles).subscribe({
      next: () => {
        this.deletingProductId.set(null);
        this.loadProducts();
      },
      error: (error: unknown) => {
        this.deletingProductId.set(null);
        if (!cascadeBundles && this.isConflict(error)) {
          this.dialog
            .open(ProductDeleteDialog, {
              data: {
                title: $localize`:@@products.delete.bundleTitle:Le produit fait partie d'un ensemble`,
                message: $localize`:@@products.delete.bundleConfirm:${this.productName(product)}:name: fait partie d'un ou plusieurs ensembles de produits. Sa suppression supprimera aussi définitivement ces ensembles. Voulez-vous continuer?`,
                confirmLabel: $localize`:@@products.delete.productAndBundles:Supprimer le produit et les ensembles`,
              },
              width: '620px',
              maxWidth: '95vw',
            })
            .afterClosed()
            .subscribe((confirmed?: boolean) => {
              if (confirmed) {
                this.deleteProduct(product, true);
              }
            });
          return;
        }

        this.deleteErrorMessage.set(
          isApiError(error)
            ? error.message
            : $localize`:@@products.errors.delete:Impossible de supprimer le produit.`,
        );
      },
    });
  }

  protected productName(product: Product): string {
    return localizedProductName(product, this.locale);
  }

  protected productDescription(product: Product): string {
    return localizedProductDescription(product, this.locale);
  }

  protected productType(type: string): string {
    return localizedProductType(type);
  }

  protected productStatus(status: Product['productStatus']): string {
    return localizedProductStatus(status);
  }

  protected deliveryType(type: Product['deliveryType']): string {
    return localizedDeliveryType(type);
  }

  private isConflict(error: unknown): boolean {
    return (
      (error instanceof HttpErrorResponse && error.status === 409) ||
      (isApiError(error) && String(error.code) === '409')
    );
  }

  protected handleSelectedTabChange(event: MatTabChangeEvent): void {
    sessionStorage.setItem('productSelectedTabIndex', event.index.toString());
  }
}
