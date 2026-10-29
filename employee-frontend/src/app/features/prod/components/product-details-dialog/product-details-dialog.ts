import { CurrencyPipe } from '@angular/common';
import { Component, inject, LOCALE_ID, OnInit, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { isApiError } from '@core/models/api-error';
import { FileDetails } from '@features/prod/models/image.model';
import {
  localizedDeliveryType,
  localizedProductDescription,
  localizedProductName,
  localizedProductStatus,
  localizedProductType,
} from '@features/prod/models/product-localization';
import { Product } from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';

interface ProductDetailsData {
  productId: string;
}

@Component({
  selector: 'app-product-details-dialog',
  imports: [CurrencyPipe, MatDialogModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './product-details-dialog.html',
  styleUrl: './product-details-dialog.css',
})
export class ProductDetailsDialog implements OnInit {
  private readonly data = inject<ProductDetailsData>(MAT_DIALOG_DATA);
  private readonly productService = inject(ProductService);
  private readonly imageService = inject(ImageService);
  private readonly locale = inject(LOCALE_ID);

  protected readonly product = signal<Product | null>(null);
  protected readonly image = signal<FileDetails | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.productService.getProduct(this.data.productId).subscribe({
      next: (product) => {
        this.product.set(product);
        this.isLoading.set(false);
        if (product.image?.fileData) {
          this.image.set(product.image);
        }
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          isApiError(error)
            ? error.message
            : $localize`:@@products.errors.details:Impossible de charger les détails du produit.`,
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
}
