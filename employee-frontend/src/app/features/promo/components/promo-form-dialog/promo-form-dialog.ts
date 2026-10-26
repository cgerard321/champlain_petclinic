import { Component, inject, OnInit, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { isApiError } from '@core/models/api-error';
import { Product, ProductType } from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';
import { Promo } from '@features/promo/models/promo.model';
import { PromoService } from '@features/promo/services/promo.service';

interface PromoFormData {
  promo?: Promo;
}

interface PromoFormModel {
  name: string;
  code: string;
  discount: number;
  expirationDate: string;
  active: boolean;
  scope: 'CART' | 'PRODUCT' | 'PRODUCT_TYPE';
  productIds: string[];
  productTypes: string[];
}

@Component({
  selector: 'app-promo-form-dialog',
  imports: [
    FormField,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
    MatSelectModule,
  ],
  templateUrl: './promo-form-dialog.html',
  styleUrl: './promo-form-dialog.css',
})
export class PromoFormDialog implements OnInit {
  private readonly productService = inject(ProductService);
  protected readonly products = signal<Product[]>([]);
  protected readonly productTypes = signal<ProductType[]>([]);

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (products) => this.products.set(products),
      error: () => this.errorMessage.set('Could not load eligible products.'),
    });
    this.productService.getProductEnums().subscribe({
      next: (enums) => this.productTypes.set(enums.productType),
      error: () => this.errorMessage.set('Could not load product types.'),
    });
  }

  protected setScope(scope: 'CART' | 'PRODUCT' | 'PRODUCT_TYPE'): void {
    this.model.update((current) => ({ ...current, scope, productIds: [], productTypes: [] }));
  }

  protected setProducts(productIds: string[]): void {
    this.model.update((current) => ({ ...current, productIds }));
  }

  protected setProductTypes(productTypes: string[]): void {
    this.model.update((current) => ({ ...current, productTypes }));
  }

  private readonly data = inject<PromoFormData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<PromoFormDialog, Promo>);
  private readonly promoService = inject(PromoService);

  protected readonly promo = this.data.promo;
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly model = signal<PromoFormModel>({
    name: this.promo?.name ?? '',
    code: this.promo?.code ?? '',
    discount: this.promo?.discount ?? 0,
    expirationDate: this.promo?.expirationDate.slice(0, 10) ?? '',
    active: this.promo?.active ?? true,
    scope: this.promo?.scope ?? 'CART',
    productIds: this.promo?.productIds ?? [],
    productTypes: this.promo?.productTypes ?? [],
  });

  protected readonly promoForm = form(this.model, (schemaPath) => {
    required(schemaPath.name, { message: 'Name is required' });
    required(schemaPath.code, { message: 'Code is required' });
    required(schemaPath.expirationDate, { message: 'Expiration date is required' });
  });

  protected setActive(active: boolean): void {
    this.model.update((current) => ({ ...current, active }));
  }

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.promoForm().invalid()) {
      return;
    }
    if (this.model().discount <= 0 || this.model().discount > 100) {
      this.errorMessage.set('Discount must be between 0 and 100.');
      return;
    }

    const { scope, productIds, productTypes } = this.model();
    if (
      (scope === 'PRODUCT' && productIds.length === 0) ||
      (scope === 'PRODUCT_TYPE' && productTypes.length === 0)
    ) {
      this.errorMessage.set('Select at least one eligible product or product type.');
      return;
    }
    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    const request = { ...this.model(), expirationDate: `${this.model().expirationDate}T23:59:59` };
    const save = this.promo
      ? this.promoService.updatePromo(this.promo.id, request)
      : this.promoService.createPromo(request);

    save.subscribe({
      next: (saved) => {
        this.isSubmitting.set(false);
        this.dialogRef.close(saved);
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(isApiError(error) ? error.message : 'Could not save the promo.');
      },
    });
  }

  protected cancel(): void {
    if (!this.isSubmitting()) {
      this.dialogRef.close();
    }
  }
}
