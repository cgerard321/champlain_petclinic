import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';

import { isApiError } from '@core/models/api-error';
import { Product } from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';
import { PromoFormDialog } from '@features/promo/components/promo-form-dialog/promo-form-dialog';
import { Promo } from '@features/promo/models/promo.model';
import { PromoService } from '@features/promo/services/promo.service';

@Component({
  imports: [
    DatePipe,
    CurrencyPipe,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
  ],
  selector: 'app-promo',
  styleUrl: './promo.css',
  templateUrl: './promo.html',
})
export class PromoList implements OnInit {
  private readonly promoService = inject(PromoService);
  private readonly dialog = inject(MatDialog);
  private readonly productService = inject(ProductService);
  protected readonly products = signal<Product[]>([]);

  protected eligibility(promo: Promo): string {
    if (promo.scope === 'PRODUCT') {
      return `Products: ${(promo.productIds ?? [])
        .map((id) => this.products().find((product) => product.productId === id)?.productName ?? id)
        .join(', ')}`;
    }
    return promo.scope === 'PRODUCT_TYPE'
      ? `Product types: ${(promo.productTypes ?? []).join(', ')}`
      : 'Full cart';
  }

  protected readonly columns = [
    'name',
    'code',
    'discount',
    'eligibility',
    'expirationDate',
    'status',
    'actions',
  ];
  protected readonly promos = signal<Promo[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadPromos();
    this.productService.getProducts().subscribe({
      next: (products) => this.products.set(products),
      error: () => this.products.set([]),
    });
  }

  protected loadPromos(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.promoService.getPromos().subscribe({
      next: (promos) => {
        this.promos.set(promos);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.errorMessage.set(isApiError(error) ? error.message : 'Could not load promos.');
      },
    });
  }

  protected openPromoForm(promo?: Promo): void {
    this.dialog
      .open(PromoFormDialog, { data: { promo }, width: '560px', maxWidth: '95vw' })
      .afterClosed()
      .subscribe((saved?: Promo) => {
        if (saved) {
          this.loadPromos();
        }
      });
  }

  protected deletePromo(promo: Promo): void {
    if (!confirm(`Delete promo "${promo.name}"?`)) {
      return;
    }

    this.promoService.deletePromo(promo.id).subscribe({
      next: () => this.loadPromos(),
      error: (error: unknown) =>
        this.errorMessage.set(isApiError(error) ? error.message : 'Could not delete the promo.'),
    });
  }
}
