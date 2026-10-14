import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';

import { SupplyAuditEntry } from '@features/supplies/models/supply';
import { SupplyService } from '@features/supplies/services/supply-service';

export interface SupplyHistoryDialogData {
  inventoryId: string;
  productId: string;
  supplyName: string;
}

@Component({
  imports: [DatePipe, MatDialogTitle, MatDialogContent, MatDialogActions],
  selector: 'app-supply-history-dialog',
  templateUrl: './supply-history-dialog.html',
  styleUrl: './supply-history-dialog.css',
})
export class SupplyHistoryDialog {
  protected readonly data = inject<SupplyHistoryDialogData>(MAT_DIALOG_DATA);
  private readonly service = inject(SupplyService);
  private readonly dialogRef = inject(MatDialogRef<SupplyHistoryDialog>);

  protected readonly loading = signal(true);
  protected readonly entries = signal<SupplyAuditEntry[]>([]);
  protected readonly error = signal(false);

  constructor() {
    this.service.getSupplyHistory(this.data.inventoryId, this.data.productId).subscribe({
      next: (entries) => {
        this.entries.set(entries);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  protected close(): void {
    this.dialogRef.close();
  }
}
