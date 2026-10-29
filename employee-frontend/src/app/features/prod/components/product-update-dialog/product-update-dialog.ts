import { Component, inject, OnInit, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { of, switchMap } from 'rxjs';

import { isApiError } from '@core/models/api-error';
import { FileDetails } from '@features/prod/models/image.model';
import {
  localizedDeliveryType,
  localizedProductType,
} from '@features/prod/models/product-localization';
import {
  DeliveryType,
  Product,
  ProductEnums,
  ProductRequest,
  ProductType,
} from '@features/prod/models/product.model';
import { ImageService } from '@features/prod/services/image.service';
import { ProductService } from '@features/prod/services/product.service';

interface ProductUpdateData {
  product: Product;
}

interface ProductFormModel {
  productName: string;
  productDescription: string;
  productNameFr: string;
  productDescriptionFr: string;
  productSalePrice: number;
  productQuantity: number;
  isUnlisted: boolean;
  productTypeId: string;
  releaseDate: string;
  deliveryType: DeliveryType;
}

@Component({
  selector: 'app-product-update-dialog',
  imports: [
    FormField,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './product-update-dialog.html',
  styleUrl: './product-update-dialog.css',
})
export class ProductUpdateDialog implements OnInit {
  private readonly data = inject<ProductUpdateData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ProductUpdateDialog, Product>);
  private readonly productService = inject(ProductService);
  private readonly imageService = inject(ImageService);

  protected readonly productTypes = signal<ProductType[]>([]);
  protected readonly deliveryTypes = signal<DeliveryType[]>(Object.values(DeliveryType));
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly selectedFile = signal<File | null>(null);
  protected readonly fileError = signal<string | null>(null);
  protected readonly originalProduct = this.data.product;

  protected readonly model = signal<ProductFormModel>({
    productName: this.data.product.productName,
    productDescription: this.data.product.productDescription,
    productNameFr: this.data.product.productNameFr ?? '',
    productDescriptionFr: this.data.product.productDescriptionFr ?? '',
    productSalePrice: this.data.product.productSalePrice,
    productQuantity: this.data.product.productQuantity,
    isUnlisted: this.data.product.isUnlisted,
    productTypeId: this.data.product.productTypeId,
    releaseDate: this.data.product.releaseDate ?? '',
    deliveryType: this.data.product.deliveryType,
  });

  protected readonly productForm = form(this.model, (schemaPath) => {
    required(schemaPath.productName, {
      message: $localize`:@@products.validation.nameRequired:Le nom du produit est requis.`,
    });
    required(schemaPath.productDescription, {
      message: $localize`:@@products.validation.descriptionRequired:La description du produit est requise.`,
    });
    required(schemaPath.productSalePrice, {
      message: $localize`:@@products.validation.priceRequired:Le prix de vente est requis.`,
    });
    required(schemaPath.productQuantity, {
      message: $localize`:@@products.validation.quantityRequired:La quantité est requise.`,
    });
    required(schemaPath.productTypeId, {
      message: $localize`:@@products.validation.typeRequired:Le type de produit est requis.`,
    });
  });

  ngOnInit(): void {
    this.productService.getProductEnums().subscribe({
      next: (enums: ProductEnums) => {
        this.productTypes.set(enums.productType);
        this.deliveryTypes.set(enums.deliveryType);
      },
      error: (error: unknown) => this.handleError(error),
    });
  }

  protected selectFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(null);
    this.fileError.set(null);

    if (!file) {
      return;
    }

    if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
      this.fileError.set(
        $localize`:@@products.validation.imageType:Seules les images JPEG, JPG et PNG sont prises en charge.`,
      );
      input.value = '';
      return;
    }

    this.selectedFile.set(file);
  }

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.productForm().invalid() || !this.validateBusinessRules()) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    const file = this.selectedFile();
    if (file) {
      void this.imageService
        .toFileDetails(file)
        .then((image) => this.updateProduct(image))
        .catch((error: unknown) => this.handleError(error));
      return;
    }

    this.updateProduct();
  }

  protected cancel(): void {
    if (!this.isSubmitting()) {
      this.dialogRef.close();
    }
  }

  private updateProduct(image?: FileDetails): void {
    const { releaseDate, ...formValue } = this.model();
    const request: ProductRequest = {
      ...formValue,
      ...(releaseDate ? { releaseDate } : {}),
      productStatus: this.originalProduct.productStatus,
    };

    this.productService
      .updateProduct(this.originalProduct.productId, request)
      .pipe(
        switchMap((product) =>
          image ? this.productService.updateProductImage(product.productId, image) : of(product),
        ),
      )
      .subscribe({
        next: (product) => {
          this.isSubmitting.set(false);
          this.dialogRef.close(product);
        },
        error: (error: unknown) => this.handleError(error),
      });
  }

  private validateBusinessRules(): boolean {
    const current = this.model();
    if (current.productSalePrice <= 0) {
      this.errorMessage.set(
        $localize`:@@products.validation.pricePositive:Le prix de vente doit être supérieur à zéro.`,
      );
      return false;
    }
    if (current.productQuantity < 0) {
      this.errorMessage.set(
        $localize`:@@products.validation.quantityNonNegative:La quantité ne peut pas être négative.`,
      );
      return false;
    }
    return !this.fileError();
  }

  private handleError(error: unknown): void {
    this.isSubmitting.set(false);
    this.errorMessage.set(
      isApiError(error)
        ? error.message
        : $localize`:@@products.errors.update:Impossible de modifier le produit.`,
    );
  }

  protected productTypeLabel(type: string): string {
    return localizedProductType(type);
  }

  protected deliveryTypeLabel(type: DeliveryType): string {
    return localizedDeliveryType(type);
  }
}
