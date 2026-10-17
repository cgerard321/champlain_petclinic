import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButton } from '@angular/material/button';
import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle,
} from '@angular/material/card';

import { AuthState } from '@core/services/auth-state';
import { Bundle } from '@features/prod/models/bundle.model';
import { BundleService } from '@features/prod/services/bundle.service';
import { Roles } from '@shared/models/roles';
import { ProductService } from '@features/prod/services/product.service';
import { Product } from '@features/prod/models/product.model';
import { forkJoin } from 'rxjs';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ProductThumbnail } from '@features/prod/components/product-thumbnail/product-thumbnail';
import { environment } from '@environments/environment';

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
  private readonly bundlesService = inject(BundleService);
  private readonly productService = inject(ProductService);
  protected readonly products = signal<Product[]>([]);
  protected readonly rawBundles = signal<Bundle[]>([]);
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
  protected openAddBundle(): void {
    return;
  }
  protected deleteBundle(bundleId: string): void {

  }

  ngOnInit(): void {
    this.loadBundles();
  }
  protected loadBundles(): void {
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
        this.bundlesError.set('Failed to load bundles.');
        this.isLoading.set(false);
      },
    });
  }
}
