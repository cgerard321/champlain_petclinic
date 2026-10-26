import { inject, Injectable, LOCALE_ID, signal } from '@angular/core';

import { ProductService } from '@features/prod/services/product.service';

// french product names from products api
@Injectable({ providedIn: 'root' })
export class ProductNameService {
  private readonly productService = inject(ProductService);
  private readonly locale = inject(LOCALE_ID);

  private readonly frenchNames = signal<Record<string, string>>({});

  // already requested
  private readonly requestedIds = new Set<string>();

  load(productIds: string[]): void {
    if (!this.isFrench()) {
      return;
    }

    for (const productId of productIds) {
      if (this.requestedIds.has(productId)) {
        continue;
      }
      this.requestedIds.add(productId);

      this.productService.getProduct(productId).subscribe({
        next: (product) => {
          const frenchName = product.productNameFr?.trim();
          if (frenchName) {
            this.frenchNames.update((names) => ({ ...names, [productId]: frenchName }));
          }
        },
        error: () => {
          // retry later
          this.requestedIds.delete(productId);
        },
      });
    }
  }

  // fallback: order name
  name(productId: string, fallbackName: string): string {
    const frenchName = this.frenchNames()[productId];
    if (frenchName) {
      return frenchName;
    }
    return fallbackName;
  }

  private isFrench(): boolean {
    return this.locale.toLowerCase().startsWith('fr');
  }
}
