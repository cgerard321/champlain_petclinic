import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';

import { Supply } from '@features/supplies/models/supply';

export type SupplySummaryAction = 'edit' | 'delete' | 'history' | 'consume';

export interface SupplySummaryDialogData {
  supply: Supply;
  displayName: string;
  displayDescription: string;
  statusLabel: string;
  photoUrl: string | null;
  canManage: boolean;
  canConsume: boolean;
  consumeDisabled: boolean;
}

@Component({
  imports: [CurrencyPipe, DatePipe, MatDialogTitle, MatDialogContent, MatDialogActions],
  selector: 'app-supply-summary-dialog',
  templateUrl: './supply-summary-dialog.html',
  styleUrl: './supply-summary-dialog.css',
})
export class SupplySummaryDialog {
  protected readonly data = inject<SupplySummaryDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<SupplySummaryDialog>);
  protected readonly notRecorded = $localize`:@@supply.supplier.notRecorded:Non indiqué`;

  protected chooseAction(action: SupplySummaryAction): void {
    this.dialogRef.close(action);
  }

  protected close(): void {
    this.dialogRef.close();
  }
}
