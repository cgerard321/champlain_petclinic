import { formatDate, DatePipe } from '@angular/common';
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
import { translateVisitType } from '@features/bill/utils/bill-translations';

@Component({
  selector: 'app-bill-update',
  imports: [
    FormField,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    DatePipe,
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

  protected readonly translateVisit = translateVisitType;

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
    required(schemaPath.visitType, {
      message: $localize`:@@bill.message.vistReq:Type de visite est requis`,
    });
    required(schemaPath.amount, {
      message: $localize`:@@bill.message.amtReq:Le montant est requis`,
    });
    required(schemaPath.dueDate, {
      message: $localize`:@@bill.message.dueDateReq:La date d'échéance est requise`,
    });
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
      this.errorMessage.set(
        $localize`:@@bill.message.selectVist:Veuillez sélectionner un type de visite valide.`,
      );
      return;
    }

    if (this.billForm().invalid()) {
      this.errorMessage.set(
        $localize`:@@bill.message.completeForm:Veuillez remplir tous les champs obligatoires`,
      );
      return;
    }

    if (!Number.isFinite(request.amount) || request.amount <= 0) {
      this.errorMessage.set(
        $localize`:@@bill.message.greatAmtReq:Le montant doit être supérieur à 0`,
      );
      return;
    }

    if (request.dueDate < request.date) {
      this.errorMessage.set(
        $localize`:@@bill.message.dueDateInvariant:La date d'échéance ne peut pas être antérieure à la date de création`,
      );
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
          this.errorMessage.set(
            $localize`:@@bill.updateBill.noPerm:Vous n'avez pas la permission de modifier cette facture`,
          );
        } else if (error instanceof HttpErrorResponse && error.status === 404) {
          this.errorMessage.set($localize`:@@bill.updateBill.notFound:Facture introuvable`);
        } else if (error instanceof HttpErrorResponse && error.status === 400) {
          this.errorMessage.set(
            $localize`:@@bill.updateBill.reject:La modification a été refusée. Veuillez vérifier les informations de la facture`,
          );
        } else {
          this.errorMessage.set(
            $localize`:@@bill.updateBill.unable:Impossible de modifier la facture. Veuillez réessayer`,
          );
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
