import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { form, FormField, required, requiredError, validate } from '@angular/forms/signals';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatChipGrid, MatChipRemove, MatChipRow } from '@angular/material/chips';
import { ErrorStateMatcher } from '@angular/material/core';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import {
  MatError,
  MatFormField,
  MatInput,
  MatLabel,
  MatPrefix,
  MatSuffix,
} from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatTab, MatTabChangeEvent, MatTabGroup } from '@angular/material/tabs';

import { isApiError } from '@core/models/api-error';
import {
  ProductSelectorDialog,
  ProductSelectorDialogData,
} from '@features/prod/components/product-selector-dialog/product-selector-dialog';
import { Bundle, BundleRequest } from '@features/prod/models/bundle.model';
import { Product } from '@features/prod/models/product.model';
import { BundleService } from '@features/prod/services/bundle.service';

interface ProductBundleFormModel {
  bundleName: string;
  bundleDescription: string;
  bundleOriginalPrice: number;
  bundlePrice: number;
  productIds: Set<string>;
}

export interface ProductBundleAddDialogData {
  products: Product[];
}

@Component({
  imports: [
    CurrencyPipe,
    MatButton,
    MatChipGrid,
    MatChipRemove,
    MatChipRow,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle,
    MatError,
    MatFormField,
    MatIcon,
    MatIconButton,
    MatInput,
    MatLabel,
    MatProgressSpinner,
    MatPrefix,
    MatSuffix,
    FormField,
    MatTabGroup,
    MatTab,
  ],
  selector: 'app-product-bundle-add-dialog',
  styleUrl: './product-bundle-add-dialog.css',
  templateUrl: './product-bundle-add-dialog.html',
})
export class ProductBundleAddDialog {
  private readonly data = inject<ProductBundleAddDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ProductBundleAddDialog, Bundle>);
  private readonly dialog = inject(MatDialog);
  private readonly bundleService = inject(BundleService);
  private readonly discountPercentage = signal<number>(0);
  private readonly discountManual = signal<number>(0);
  protected readonly usePercentageDiscount = signal<boolean>(false);
  private readonly itemsChipGrid = viewChild.required(MatChipGrid);
  private readonly percentageInput = viewChild('percentageInput', { read: MatInput });
  private readonly percentageTouched = signal(false);
  private readonly manualInput = viewChild('manualInput', { read: MatInput });
  private readonly manualTouched = signal(false);
  protected readonly selectedProductIds = signal<Set<string>>(new Set());
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly model = signal<ProductBundleFormModel>({
    bundleName: '',
    bundleDescription: '',
    bundleOriginalPrice: 0,
    bundlePrice: 0,
    productIds: this.selectedProductIds(),
  });

  protected readonly bundleForm = form(this.model, (schemaPath) => {
    required(schemaPath.bundleName, { message: 'Bundle name is required' });
    required(schemaPath.bundleDescription, { message: 'Bundle description is required' });
    required(schemaPath.bundlePrice, { message: 'Bundle price is required' });
    validate(schemaPath.productIds, ({ value }) =>
      value().size === 0 ? requiredError({ message: 'Bundle needs at least one item' }) : undefined,
    );
  });

  protected readonly bundlePriceDisplay = computed(() => {
    const price = this.model().bundlePrice;
    const original = this.model().bundleOriginalPrice;
    return Number.isFinite(price) && price > 0 && price < original ? price : null;
  });

  protected readonly bundleSavings = computed(() => {
    const price = this.bundlePriceDisplay();
    return price === null ? null : Number((this.model().bundleOriginalPrice - price).toFixed(2));
  });

  protected readonly itemsErrorStateMatcher: ErrorStateMatcher = {
    isErrorState: () =>
      this.bundleForm.productIds().touched() && this.bundleForm.productIds().invalid(),
  };

  protected readonly percentageError = computed(() => {
    const percentage = this.discountPercentage();
    if (Number.isNaN(percentage)) return 'Discount percentage is required';
    if (percentage < 1) return 'Discount must be at least 1%';
    if (percentage > 100) return 'Discount cannot be more than 100%';
    return null;
  });

  protected readonly percentageErrorStateMatcher: ErrorStateMatcher = {
    isErrorState: () => this.percentageTouched() && this.percentageError() !== null,
  };

  protected readonly manualError = computed(() => {
    const price = this.discountManual();
    const original = this.model().bundleOriginalPrice;
    if (Number.isNaN(price)) return 'Bundle price is required';
    if (price <= 0) return 'Bundle price must be greater than $0';
    if (price >= original) return `Bundle price must be less than $${original.toFixed(2)}`;
    return null;
  });

  protected readonly manualErrorStateMatcher: ErrorStateMatcher = {
    isErrorState: () => this.manualTouched() && this.manualError() !== null,
  };

  private validateBusinessRules(): boolean {
    const current = this.model();
    if (current.bundlePrice <= 0) {
      this.errorMessage.set('Bundle price must be greater than zero.');
      return false;
    }
    if (current.bundlePrice >= current.bundleOriginalPrice) {
      this.errorMessage.set('Bundle price must be less than the original price.');
      return false;
    }
    if (current.productIds.size < 1) {
      this.errorMessage.set('Cannot have less than one item in bundle.');
      return false;
    }
    return true;
  }

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.bundleForm().invalid() || !this.validateBusinessRules()) {
      this.bundleForm().markAsTouched();
      this.itemsChipGrid().updateErrorState();
      this.percentageTouched.set(true);
      this.percentageInput()?.updateErrorState();
      this.manualTouched.set(true);
      this.manualInput()?.updateErrorState();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.createBundle();
  }

  private createBundle(): void {
    const request: BundleRequest = {
      ...this.model(),
      productIds: Array.from(this.model().productIds.values()),
    };
    this.bundleService.createBundle(request).subscribe({
      next: (bundle: Bundle) => {
        this.isSubmitting.set(false);
        this.dialogRef.close(bundle);
      },
      error: (error) => {
        this.handleError(error);
      },
    });
  }

  private handleError(error: unknown): void {
    this.isSubmitting.set(false);
    this.errorMessage.set(isApiError(error) ? error.message : 'Could not save the bundle.');
  }

  protected cancel(): void {
    if (!this.isSubmitting()) {
      this.dialogRef.close();
    }
  }

  protected openProductSelector(): void {
    this.dialog
      .open<ProductSelectorDialog, ProductSelectorDialogData>(ProductSelectorDialog, {
        data: {
          alreadySelected: this.selectedProductIds(),
          products: this.data.products,
        },
      })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.setSelectedProductIds(new Set(result));
        }
        this.bundleForm.productIds().markAsTouched();
        this.itemsChipGrid().updateErrorState();
      });
  }

  protected removeProduct(productId: string): void {
    const next = new Set(this.selectedProductIds());
    next.delete(productId);
    this.setSelectedProductIds(next);
  }
  private setSelectedProductIds(ids: Set<string>): void {
    this.selectedProductIds.set(ids);
    this.model.update((current) => ({
      ...current,
      productIds: ids,
      bundleOriginalPrice: this.getBundleOriginalPrice(),
    }));
    this.itemsChipGrid().updateErrorState();
    this.manualInput()?.updateErrorState();

    this.calculateBundleDiscountPrice();
  }

  protected getProductByProductId(id: string): Product | undefined {
    return this.data.products.find((product: Product) => product.productId === id);
  }

  protected getBundleOriginalPrice(): number {
    let total: number = 0;
    this.selectedProductIds().forEach((productId: string) => {
      const prod = this.getProductByProductId(productId);
      if (prod) total += prod.productSalePrice;
    });
    return Number(total.toFixed(2));
  }

  protected handleManualBundlePrice(event: Event): void {
    const price = (event.target as HTMLInputElement).valueAsNumber;
    this.discountManual.set(Number.isNaN(price) ? price : Number(price.toFixed(2)));
    this.usePercentageDiscount.set(false);
    this.manualTouched.set(true);
    this.manualInput()?.updateErrorState();

    this.calculateBundleDiscountPrice();
  }

  protected handlePercentageBundlePrice(event: Event): void {
    const percentage = (event.target as HTMLInputElement).valueAsNumber;
    this.usePercentageDiscount.set(true);
    this.discountPercentage.set(percentage);
    this.percentageTouched.set(true);
    this.percentageInput()?.updateErrorState();

    this.calculateBundleDiscountPrice();
  }

  protected calculateBundleDiscountPrice(): void {
    let discountPrice = this.getBundleOriginalPrice();
    if (this.usePercentageDiscount() && this.percentageError()) discountPrice = NaN;
    else if (this.usePercentageDiscount())
      discountPrice = Number((discountPrice * (1 - this.discountPercentage() / 100)).toFixed(2));
    else if (this.manualError()) discountPrice = NaN;
    else discountPrice = this.discountManual();

    this.model.update((current) => ({
      ...current,
      bundlePrice: discountPrice,
    }));
  }

  protected handleTabChangeBundleDiscount(event: MatTabChangeEvent, percentageTab: MatTab): void {
    this.usePercentageDiscount.set(event.tab === percentageTab);
    this.calculateBundleDiscountPrice();
  }
}
