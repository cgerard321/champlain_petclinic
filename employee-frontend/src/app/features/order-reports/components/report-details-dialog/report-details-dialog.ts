import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

import { OrderDetailsDialog } from '@features/order-reports/components/order-details-dialog/order-details-dialog';
import {
  OrderReport,
  REPORT_REASON_LABELS,
  REPORT_STATUS_LABELS,
} from '@features/order-reports/models/order-report.model';
import { ProductNameService } from '@features/order-reports/services/product-name.service';

interface ReportDetailsData {
  report: OrderReport;
}

@Component({
  selector: 'app-report-details-dialog',
  imports: [DatePipe, MatButtonModule, MatDialogModule, MatIconModule],
  templateUrl: './report-details-dialog.html',
  styleUrl: './report-details-dialog.css',
})
export class ReportDetailsDialog {
  private readonly data = inject<ReportDetailsData>(MAT_DIALOG_DATA);
  private readonly dialog = inject(MatDialog);
  private readonly dialogRef = inject(MatDialogRef<ReportDetailsDialog>);
  private readonly destroyRef = inject(DestroyRef);
  private orderDialogRef: MatDialogRef<OrderDetailsDialog> | null = null;
  protected readonly productNames = inject(ProductNameService);

  protected readonly report = this.data.report;
  protected readonly statusLabels = REPORT_STATUS_LABELS;
  protected readonly reasonLabels = REPORT_REASON_LABELS;

  constructor() {
    this.productNames.load(this.report.affectedItems.map((item) => item.productId));

    // If the report closes while the order is still open, center the order again
    // and give the order its own grey background right away.
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

  protected openOrder(): void {
    if (this.orderDialogRef) {
      return;
    }

    // Slide this dialog to the left and open the order on the right, instead of stacking them.
    this.dialogRef.addPanelClass(['dialog-shift-left', 'dialogs-side-by-side']);
    this.orderDialogRef = this.dialog.open(OrderDetailsDialog, {
      data: { orderId: this.report.orderId },
      width: '820px',
      maxWidth: '95vw',
      autoFocus: 'dialog',
      panelClass: ['order-dialog-pane', 'dialog-slide-right'],
      // Setting backdropClass replaces Material's default, so the dark backdrop is listed again.
      backdropClass: ['cdk-overlay-dark-backdrop', 'order-dialog-backdrop'],
    });

    // When the order closes, center this dialog again.
    this.orderDialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.orderDialogRef = null;
        this.dialogRef.removePanelClass(['dialog-shift-left', 'dialogs-side-by-side']);
      });
  }
}
