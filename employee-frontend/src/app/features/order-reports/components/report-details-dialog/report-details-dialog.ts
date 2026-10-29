import { DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { isApiError } from '@core/models/api-error';
import { OrderDetailsDialog } from '@features/order-reports/components/order-details-dialog/order-details-dialog';
import { ReportStatusDialog } from '@features/order-reports/components/report-status-dialog/report-status-dialog';
import {
  OrderReport,
  OrderReportStatus,
  REPORT_REASON_LABELS,
  REPORT_STATUS_LABELS,
} from '@features/order-reports/models/order-report.model';
import { OrderReportService } from '@features/order-reports/services/order-report.service';
import { ProductNameService } from '@features/order-reports/services/product-name.service';

interface ReportDetailsData {
  report: OrderReport;
  onUpdated: (report: OrderReport) => void;
}

@Component({
  selector: 'app-report-details-dialog',
  imports: [DatePipe, MatButtonModule, MatDialogModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './report-details-dialog.html',
  styleUrl: './report-details-dialog.css',
})
export class ReportDetailsDialog {
  private readonly data = inject<ReportDetailsData>(MAT_DIALOG_DATA);
  private readonly dialog = inject(MatDialog);
  private readonly dialogRef = inject(MatDialogRef<ReportDetailsDialog>);
  private readonly destroyRef = inject(DestroyRef);
  private readonly orderReportService = inject(OrderReportService);
  private orderDialogRef: MatDialogRef<OrderDetailsDialog> | null = null;
  protected readonly productNames = inject(ProductNameService);

  protected readonly report = signal(this.data.report);
  protected readonly statusLabels = REPORT_STATUS_LABELS;
  protected readonly reasonLabels = REPORT_REASON_LABELS;

  protected readonly isUpdating = signal(false);
  protected readonly updateError = signal<string | null>(null);

  // new or in review
  protected readonly isOpen = computed(() => {
    const status = this.report().status;
    return status === 'NEW' || status === 'IN_REVIEW';
  });

  constructor() {
    this.productNames.load(this.report().affectedItems.map((item) => item.productId));

    // report closed: re-center order
    this.dialogRef
      .beforeClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (this.orderDialogRef) {
          this.dialogRef.removePanelClass('dialogs-side-by-side');
          this.orderDialogRef.removePanelClass('dialog-slide-right');
        }
      });
  }

  protected markInReview(): void {
    this.updateStatus('IN_REVIEW', null);
  }

  protected resolve(): void {
    this.confirmStatus('RESOLVED');
  }

  protected reject(): void {
    this.confirmStatus('REJECTED');
  }

  // confirm, then save
  private confirmStatus(action: 'RESOLVED' | 'REJECTED'): void {
    this.dialog
      .open(ReportStatusDialog, {
        data: { action },
        width: '520px',
        maxWidth: '95vw',
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((note: string | undefined) => {
        if (note === undefined) {
          return;
        }
        if (note === '') {
          this.updateStatus(action, null);
        } else {
          this.updateStatus(action, note);
        }
      });
  }

  private updateStatus(status: OrderReportStatus, note: string | null): void {
    this.isUpdating.set(true);
    this.updateError.set(null);
    this.orderReportService
      .updateStatus(this.report().reportId, { status, resolutionNote: note })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updatedReport) => {
          this.report.set(updatedReport);
          this.isUpdating.set(false);
          this.data.onUpdated(updatedReport);
        },
        error: (error: unknown) => {
          this.isUpdating.set(false);
          if (isApiError(error)) {
            this.updateError.set(error.message);
          } else {
            this.updateError.set(
              $localize`:@@orderReports.errors.updateFailed:Impossible de mettre à jour le statut du signalement.`,
            );
          }
        },
      });
  }

  protected openOrder(): void {
    if (this.orderDialogRef) {
      return;
    }

    // side by side, no stacking
    this.dialogRef.addPanelClass(['dialog-shift-left', 'dialogs-side-by-side']);
    this.orderDialogRef = this.dialog.open(OrderDetailsDialog, {
      data: { orderId: this.report().orderId },
      width: '820px',
      maxWidth: '95vw',
      autoFocus: 'dialog',
      panelClass: ['order-dialog-pane', 'dialog-slide-right'],
      // keep default dark backdrop
      backdropClass: ['cdk-overlay-dark-backdrop', 'order-dialog-backdrop'],
    });

    // order closed: re-center report
    this.orderDialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.orderDialogRef = null;
        this.dialogRef.removePanelClass(['dialog-shift-left', 'dialogs-side-by-side']);
      });
  }
}
