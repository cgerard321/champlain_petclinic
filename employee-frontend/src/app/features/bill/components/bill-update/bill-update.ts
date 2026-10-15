import { formatDate } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { BillRecord, BillUpdateRequest } from '@features/bill/models/bill.model';
import { BillService } from '@features/bill/services/bill-service';

@Component({
  selector: 'app-bill-update',
  imports: [
    FormField,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './bill-update.html',
  styleUrl: './bill-update.css',
})
export class BillUpdate {
  private readonly data = inject<{ bill: BillRecord }>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<BillUpdate, BillRecord>);
  private readonly billService = inject(BillService);

  protected readonly bill = this.data.bill;
  protected readonly isSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly model = signal<BillUpdateRequest>({
    customerId: this.bill.customerId,
    vetId: this.bill.vetId,
    visitType: this.bill.visitType,
    date: formatDate(this.bill.date, 'yyyy-MM-dd', 'en-US'),
    dueDate: formatDate(this.bill.dueDate, 'yyyy-MM-dd', 'en-US'),
    amount: this.bill.amount,
    billStatus: this.bill.billStatus,
  });
  protected readonly visitTypes = ['Check-Up', 'Vaccine', 'Surgery', 'Dental'];

  protected readonly billForm = form(this.model, (schemaPath) => {
    required(schemaPath.visitType, { message: 'Visit type is required' });
    required(schemaPath.amount, { message: 'Amount is required' });
    required(schemaPath.dueDate, { message: 'Due date is required' });
  });

  protected submit(event: Event): void {
    event.preventDefault();

    if (this.isSaving()) {
      return;
    }

    const request = this.model();
    const normalizedVisitType = request.visitType.replace(/[^a-z0-9]/gi, '').toUpperCase();

    const validVisitType = this.visitTypes.some(
      (type) => type.replace(/[^a-z0-9]/gi, '').toUpperCase() === normalizedVisitType,
    );

    if (!validVisitType) {
      this.errorMessage.set('Please select a valid visit type.');
      return;
    }

    if (this.billForm().invalid()) {
      this.errorMessage.set('Please complete the required fields.');
      return;
    }

    if (!Number.isFinite(request.amount) || request.amount <= 0) {
      this.errorMessage.set('Amount must be greater than zero.');
      return;
    }

    if (request.dueDate < request.date) {
      this.errorMessage.set('Due date cannot be before the creation date.');
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    this.billService.updateBill(this.bill.billId, request).subscribe({
      next: (updatedBill: BillRecord) => {
        this.isSaving.set(false);
        this.dialogRef.close(updatedBill);
      },
      error: (error: unknown) => {
        this.isSaving.set(false);

        if (error instanceof HttpErrorResponse && error.status === 403) {
          this.errorMessage.set('You do not have permission to update this bill.');
        } else if (error instanceof HttpErrorResponse && error.status === 404) {
          this.errorMessage.set('Bill not found.');
        } else if (error instanceof HttpErrorResponse && error.status === 400) {
          this.errorMessage.set('The update was rejected. Check the bill information.');
        } else {
          this.errorMessage.set('Unable to update the bill. Please try again.');
        }
      },
    });
  }

  protected cancel(): void {
    if (!this.isSaving()) {
      this.dialogRef.close();
    }
  }
}
