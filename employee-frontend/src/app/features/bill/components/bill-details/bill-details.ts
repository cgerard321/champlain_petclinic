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

  protected readonly billStatusTranslation: Record<string, string> = {
    PAID: $localize`:@@bill.paid:PAYÉE`,
    UNPAID: $localize`:@@bill.unpaid:IMPAYÉE`,
    OVERDUE: $localize`:@@bill.overdue:EN RETARD`,
  };

  protected readonly visitTypesTranslation: Record<string, string> = {
    'Check-up': $localize`:@@bill.visitTypes:Examen vétérinaire`,
    Vaccine: $localize`:@@bill.visitTypes:Vaccin`,
    Surgery: $localize`:@@bill.visitTypes:Chirurgie`,
    Dental: $localize`:@@bill.visitTypes:Dentaire`,
    Emergency: $localize`:@@bill.visitTypes:Urgence`,
    Regular: $localize`:@@bill.visitTypes:Régulier`,
  };

  protected translate(status: string): string {
    return this.billStatusTranslation[status] ?? status;
  }

  protected translateVisit(type: string): string {
    return this.visitTypesTranslation[type] ?? type;
  }

  protected close(): void {
    this.dialogRef.close(this.bill());
  }
}
