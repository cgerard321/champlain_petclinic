import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

import { BillRecord } from '@features/bill/models/bill.model';

@Component({
  selector: 'app-bill-details',
  imports: [CurrencyPipe, MatDialogModule],
  templateUrl: './bill-details.html',
  styleUrl: './bill-details.css',
})
export class BillDetails {
  private readonly dialogRef = inject(MatDialogRef<BillDetails>);

  protected readonly bill = signal(inject<BillRecord>(MAT_DIALOG_DATA));

  protected close(): void {
    this.dialogRef.close(this.bill());
  }
}
