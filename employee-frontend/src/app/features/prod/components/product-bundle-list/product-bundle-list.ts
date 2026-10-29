import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, LOCALE_ID, OnInit, signal } from '@angular/core';
import { MatButton } from '@angular/material/button';
import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle,
} from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { environment } from '@environments/environment';
import {
  ProductBundleAddDialog,
  ProductBundleAddDialogData,
} from '@features/prod/components/product-bundle-add-dialog/product-bundle-add-dialog';
import {
  ProductBundleUpdateDialog,
  ProductBundleUpdateDialogData,
} from '@features/prod/components/product-bundle-update-dialog/product-bundle-update-dialog';
import { ProductDeleteDialog } from '@features/prod/components/product-delete-dialog/product-delete-dialog';
import { ProductThumbnail } from '@features/prod/components/product-thumbnail/product-thumbnail';
import {
  localizedBundleDescription,
  localizedBundleName,
} from '@features/prod/models/bundle-localization';
import { Bundle } from '@features/prod/models/bundle.model';
import { localizedProductName } from '@features/prod/models/product-localization';
import { Product } from '@features/prod/models/product.model';
import { BundleService } from '@features/prod/services/bundle.service';
import { ProductService } from '@features/prod/services/product.service';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [
    CurrencyPipe,
    MatButton,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatCardSubtitle,
    MatCardTitle,
    MatProgressSpinner,
    ProductThumbnail,
    MatCardActions,
  ],
  selector: 'app-product-bundle-list',
  styleUrl: './product-bundle-list.css',
  templateUrl: './product-bundle-list.html',
})
export class ProductBundleList implements OnInit {
  protected readonly customerPortal = environment.customerPortal;
  private readonly auth = inject(AuthState);
  private readonly dialog = inject(MatDialog);
  private readonly bundlesService = inject(BundleService);
  private readonly productService = inject(ProductService);
  private readonly locale = inject(LOCALE_ID);
  private readonly deletingBundleId = signal<string | null>(null);
  protected readonly products = signal<Product[]>([]);
  protected readonly rawBundles = signal<Bundle[]>([]);
  protected readonly deleteErrorMessage = signal<string | null>(null);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly bundles = computed(() => {
    const byId = new Map(this.products().map((p) => [p.productId, p]));
    return this.rawBundles().map((bundle) => ({
      ...bundle,
      products: bundle.productIds
        .map((id) => byId.get(id))
        .filter((p): p is Product => p !== undefined),
    }));
  });
  protected readonly bundlesError = signal<string | null>(null);
  protected readonly canManageBundles = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin) || roles.includes(Roles.inventoryManager);
  });
  protected readonly canDeleteBundles = computed(() => {
    const roles = this.auth.roles();
    return roles.includes(Roles.admin);
  });
  protected openAddBundle(): void {
    if (!this.canManageBundles()) {
      return;
    }

    this.dialog
      .open<ProductBundleAddDialog, ProductBundleAddDialogData>(ProductBundleAddDialog, {
        autoFocus: 'first-tabbable',
        width: '850px',
        maxWidth: '95vw',
        maxHeight: '150vh',
        data: {
          products: this.products(),
        },
      })
      .afterClosed()
      .subscribe((bundle?: Bundle) => {
        if (bundle) {
          this.loadBundles();
        }
      });
    return;
  }

  protected bundleName(bundle: Bundle): string {
    return localizedBundleName(bundle.bundleName);
  }

  protected bundleDescription(bundle: Bundle): string {
    return localizedBundleDescription(bundle.bundleDescription);
  }

  protected productName(product: Product): string {
    return localizedProductName(product, this.locale);
  }
  protected openUpdateBundle(curBundle: Bundle): void {
    if (!this.canManageBundles()) {
      return;
    }

    this.dialog
      .open<ProductBundleUpdateDialog, ProductBundleUpdateDialogData>(ProductBundleUpdateDialog, {
        autoFocus: 'first-tabbable',
        width: '850px',
        maxWidth: '95vw',
        maxHeight: '150vh',
        data: {
          products: this.products(),
          bundle: curBundle,
        },
      })
      .afterClosed()
      .subscribe((bundle?: Bundle) => {
        if (bundle) {
          this.loadBundles();
        }
      });
    return;
  }
  protected deleteBundle(bundle: Bundle): void {
    this.deletingBundleId.set(bundle.bundleId);
    this.deleteErrorMessage.set(null);
    this.bundlesService.deleteBundle(bundle.bundleId).subscribe({
      next: () => {
        this.deletingBundleId.set(null);
        this.loadBundles();
      },
      error: (error) => {
        this.deletingBundleId.set(null);
        this.deleteErrorMessage.set(error.message);
      },
    });
  }

  protected openBundleDelete(bundle: Bundle): void {
    if (!this.canManageBundles() || this.deletingBundleId() || !this.canDeleteBundles()) {
      return;
    }

    this.deleteErrorMessage.set(null);
    this.dialog
      .open(ProductDeleteDialog, {
        data: {
          title: $localize`:@@products.bundles.delete.title:Supprimer l'ensemble?`,
          message: $localize`:@@products.bundles.delete.confirm:Voulez-vous vraiment supprimer ${bundle.bundleName}:name: ? Cette action est irréversible.`,
          confirmLabel: $localize`:@@products.actions.delete:Supprimer`,
        },
        width: '520px',
        maxWidth: '95vw',
      })
      .afterClosed()
      .subscribe((confirmed?: boolean) => {
        if (confirmed) {
          this.deleteBundle(bundle);
        }
      });
  }

  ngOnInit(): void {
    this.loadBundles();
  }
  protected loadBundles(): void {
    this.isLoading.set(true);
    forkJoin({
      products: this.productService.getProducts(),
      bundles: this.bundlesService.getBundles(),
    }).subscribe({
      next: ({ products, bundles }) => {
        this.rawBundles.set(bundles);
        this.products.set(products);
        this.isLoading.set(false);
      },
      error: () => {
        this.bundlesError.set(
          $localize`:@@products.bundles.errors.load:Impossible de charger les ensembles.`,
        );
        this.isLoading.set(false);
      },
    });
  }
}
