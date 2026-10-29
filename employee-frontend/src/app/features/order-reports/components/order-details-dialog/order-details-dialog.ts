import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { isApiError } from '@core/models/api-error';
import { Order } from '@features/order-reports/models/order-report.model';
import { OrderReportService } from '@features/order-reports/services/order-report.service';
import { ProductNameService } from '@features/order-reports/services/product-name.service';

interface OrderDetailsData {
  orderId: string;
}

@Component({
  selector: 'app-order-details-dialog',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './order-details-dialog.html',
  styleUrl: './order-details-dialog.css',
})
export class OrderDetailsDialog implements OnInit {
  private readonly data = inject<OrderDetailsData>(MAT_DIALOG_DATA);
  private readonly orderReportService = inject(OrderReportService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly productNames = inject(ProductNameService);

  protected readonly orderId = this.data.orderId;
  protected readonly order = signal<Order | null>(null);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.isLoading.set(true);
    this.orderReportService
      .getOrder(this.orderId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (order) => {
          this.order.set(order);
          this.productNames.load(order.products.map((product) => product.productId));
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.isLoading.set(false);
          this.errorMessage.set(
            isApiError(error)
              ? error.message
              : $localize`:@@orderReports.errors.orderLoadFailed:Impossible de charger la commande.`,
          );
        },
      });
  }
}
