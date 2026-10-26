import { DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';

import { isApiError } from '@core/models/api-error';
import { OrderDetailsDialog } from '@features/order-reports/components/order-details-dialog/order-details-dialog';
import { ReportDetailsDialog } from '@features/order-reports/components/report-details-dialog/report-details-dialog';
import {
  OrderReport,
  OrderReportReason,
  OrderReportStatus,
  REPORT_REASON_LABELS,
  REPORT_STATUS_LABELS,
} from '@features/order-reports/models/order-report.model';
import { OrderReportService } from '@features/order-reports/services/order-report.service';

// paginator i18n
function orderReportsPaginatorIntl(): MatPaginatorIntl {
  const intl = new MatPaginatorIntl();
  intl.itemsPerPageLabel = $localize`:@@orderReports.paginator.itemsPerPage:Éléments par page :`;
  intl.nextPageLabel = $localize`:@@orderReports.paginator.next:Page suivante`;
  intl.previousPageLabel = $localize`:@@orderReports.paginator.previous:Page précédente`;
  intl.firstPageLabel = $localize`:@@orderReports.paginator.first:Première page`;
  intl.lastPageLabel = $localize`:@@orderReports.paginator.last:Dernière page`;
  intl.getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0) {
      return $localize`:@@orderReports.paginator.empty:0 sur 0`;
    }
    const start = page * pageSize + 1;
    const end = Math.min(start + pageSize - 1, length);
    return $localize`:@@orderReports.paginator.range:${start}:START: – ${end}:END: sur ${length}:LENGTH:`;
  };
  return intl;
}

@Component({
  providers: [{ provide: MatPaginatorIntl, useFactory: orderReportsPaginatorIntl }],
  imports: [
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatTableModule,
  ],
  selector: 'app-order-reports',
  styleUrl: './order-reports.css',
  templateUrl: './order-reports.html',
})
export class OrderReportList implements OnInit {
  private readonly orderReportService = inject(OrderReportService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly columns = ['report', 'order', 'customer', 'reason', 'status'];

  protected readonly reports = signal<OrderReport[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly pagedReports = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.reports().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.loadReports();
  }

  protected loadReports(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.orderReportService
      .getReports()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (reports) => {
          this.reports.set(reports);
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.isLoading.set(false);
          this.errorMessage.set(
            isApiError(error)
              ? error.message
              : $localize`:@@orderReports.errors.loadFailed:Impossible de charger les signalements.`,
          );
        },
      });
  }

  protected statusLabel(status: string): string {
    return REPORT_STATUS_LABELS[status as OrderReportStatus];
  }

  protected reasonLabel(reason: string): string {
    return REPORT_REASON_LABELS[reason as OrderReportReason];
  }

  protected onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  protected openReport(report: OrderReport): void {
    // no scroll to close button
    this.dialog.open(ReportDetailsDialog, {
      data: { report },
      width: '820px',
      maxWidth: '95vw',
      autoFocus: 'dialog',
      panelClass: 'report-dialog-pane',
    });
  }

  protected openOrder(event: Event, orderId: string): void {
    // skip row click
    event.stopPropagation();
    this.dialog.open(OrderDetailsDialog, {
      data: { orderId },
      width: '820px',
      maxWidth: '95vw',
      autoFocus: 'dialog',
    });
  }
}
