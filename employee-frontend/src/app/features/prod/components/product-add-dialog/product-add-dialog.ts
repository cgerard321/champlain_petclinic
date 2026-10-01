import { Component, inject, OnInit, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { isApiError } from '@core/models/api-error';
import { FileDetails } from '@features/prod/models/image.model';
import {
  DeliveryType,
  Product,
  ProductEnums,
  ProductRequest,
  ProductType,
} from '@features/prod/models/product.model';
import { ImageService } from '@features/prod/services/image.service';
import { ProductService } from '@features/prod/services/product.service';

interface ProductFormModel {
  productName: string;
  productDescription: string;
  productSalePrice: number;
  productQuantity: number;
  isUnlisted: boolean;
  productTypeId: string;
  releaseDate: string;
  deliveryType: DeliveryType;
}

@Component({
  selector: 'app-product-add-dialog',
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
  templateUrl: './product-add-dialog.html',
  styleUrl: './product-add-dialog.css',
})
export class ProductAddDialog implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<ProductAddDialog, Product>);
  private readonly productService = inject(ProductService);
  private readonly imageService = inject(ImageService);

  protected readonly productTypes = signal<ProductType[]>([]);
  protected readonly deliveryTypes = signal<DeliveryType[]>([]);
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly selectedFile = signal<File | null>(null);
  protected readonly fileError = signal<string | null>(null);

  ngOnInit(): void {
    this.productService.getProductEnums().subscribe({
      next: (enums: ProductEnums) => {
        this.productTypes.set(enums.productType);
        this.deliveryTypes.set(enums.deliveryType);
      },
      error: (error: unknown) => this.handleError(error),
    });
  }

  protected readonly model = signal<ProductFormModel>({
    productName: '',
    productDescription: '',
    productSalePrice: 0,
    productQuantity: 0,
    isUnlisted: false,
    productTypeId: '',
    releaseDate: '',
    deliveryType: '' as DeliveryType,
  });

  protected readonly productForm = form(this.model, (schemaPath) => {
    required(schemaPath.productName, { message: 'Product name is required' });
    required(schemaPath.productDescription, { message: 'Product description is required' });
    required(schemaPath.productSalePrice, { message: 'Sale price is required' });
    required(schemaPath.productQuantity, { message: 'Quantity is required' });
    required(schemaPath.productTypeId, { message: 'Product type is required' });
  });

  protected selectFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(null);
    this.fileError.set(null);

    if (!file) {
      return;
    }

    if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
      this.fileError.set('Only JPEG, JPG, and PNG images are supported.');
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
        .then((image) => this.createProduct(image))
        .catch((error: unknown) => this.handleError(error));
      return;
    }

    this.createProduct();
  }

  protected cancel(): void {
    if (!this.isSubmitting()) {
      this.dialogRef.close();
    }
  }

  private createProduct(image?: FileDetails): void {
    const { releaseDate, ...formValue } = this.model();
    const request: ProductRequest = {
      ...formValue,
      ...(releaseDate ? { releaseDate } : {}),
      ...(image ? { image } : {}),
    };
    this.productService.createProduct(request).subscribe({
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
      this.errorMessage.set('Sale price must be greater than zero.');
      return false;
    }
    if (current.productQuantity < 0) {
      this.errorMessage.set('Quantity cannot be negative.');
      return false;
    }
    if (this.fileError()) {
      return false;
    }
    return true;
  }

  private handleError(error: unknown): void {
    this.isSubmitting.set(false);
    this.errorMessage.set(isApiError(error) ? error.message : 'Could not save the product.');
  }
}
