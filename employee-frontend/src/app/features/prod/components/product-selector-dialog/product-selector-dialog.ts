import { Component, computed, inject, signal } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatCheckbox } from '@angular/material/checkbox';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatList, MatListItem } from '@angular/material/list';

import { Product } from '@features/prod/models/product.model';

export interface ProductSelectorDialogData {
  alreadySelected: Set<string>;
  products: Product[];
}

@Component({
  imports: [
    MatCheckbox,
    MatDialogActions,
    MatDialogContent,
    MatFormField,
    MatInput,
    MatLabel,
    MatList,
    MatListItem,
    MatButton,
  ],
  selector: 'app-product-selector-dialog',
  styleUrl: './product-selector-dialog.css',
  templateUrl: './product-selector-dialog.html',
})
export class ProductSelectorDialog {
  constructor() {
    this.dialogRef.backdropClick().subscribe(() => this.close());
  }
  private readonly dialogRef = inject(MatDialogRef<ProductSelectorDialog, string[]>);
  protected readonly data = inject<ProductSelectorDialogData>(MAT_DIALOG_DATA);

  private readonly searchTerm = signal('');
  protected readonly selectedProductIds = signal<ReadonlySet<string>>(
    this.data.alreadySelected ?? new Set(),
  );

  protected readonly filteredProducts = computed(() => {
    const term = this.searchTerm().toLowerCase();
    if (!term) {
      return this.data.products;
    }
    return this.data.products.filter((product) => product.productName.toLowerCase().includes(term));
  });

  protected searchProducts(productName: string): void {
    this.searchTerm.set(productName.trim());
  }

  protected isSelected(productId: string): boolean {
    return this.selectedProductIds().has(productId);
  }

  protected toggleProduct(productId: string, checked: boolean): void {
    this.selectedProductIds.update((ids) => {
      const next = new Set(ids);
      if (checked) {
        next.add(productId);
      } else {
        next.delete(productId);
      }
      return next;
    });
  }

  protected close(): void {
    this.dialogRef.close(this.selectedProductIds());
  }

  // Closing without a result leaves the caller's existing selection untouched
  protected cancel(): void {
    this.dialogRef.close();
  }

  protected clear(): void {
    this.selectedProductIds.set(new Set([]));
  }

  protected addAll(): void {
    this.data.products.forEach((product) => {
      this.toggleProduct(product.productId, true);
    });
  }
}
