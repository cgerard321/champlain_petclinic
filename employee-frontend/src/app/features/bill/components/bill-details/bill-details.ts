import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  MatDialog,
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { finalize, switchMap, take } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { BillRecord } from '@features/bill/models/bill.model';
import { BillService } from '@features/bill/services/bill-service';
import { translateBillStatus, translateVisitType } from '@features/bill/utils/bill-translations';
import { ConfirmDialog } from '@shared/components/confirm-dialog/confirm-dialog';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-bill-details',
  imports: [CurrencyPipe, MatDialogModule, DatePipe],
  templateUrl: './bill-details.html',
  styleUrl: './bill-details.css',
})
export class BillDetails {
  private readonly dialogRef = inject(MatDialogRef<BillDetails>);
  private readonly dialog = inject(MatDialog);
  private readonly billService = inject(BillService);
  private readonly auth = inject(AuthState);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly bill = signal(inject<BillRecord>(MAT_DIALOG_DATA));

  protected readonly translateStatus = translateBillStatus;
  protected readonly translateVisit = translateVisitType;

  protected readonly updatingInterest = signal(false);
  protected readonly interestError = signal<string | null>(null);
  protected readonly interestSuccess = signal<string | null>(null);

  // Only admins can manage interest exemption on overdue bills.
  protected readonly canManageInterest = computed(
    () => this.auth.hasRole(Roles.admin) && this.bill().billStatus === 'OVERDUE',
  );

  protected toggleInterestExemption(): void {
    if (!this.canManageInterest() || this.updatingInterest()) {
      return;
    }

    const current = this.bill();
    const exempt = !current.interestExempt;

    // Require confirmation before updating the exemption.
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: $localize`:@@bill.exemption.title:Confirmer l'exemption de l'intérêt`,
          message: exempt
            ? $localize`:@@bill.exemption.exempt:Exempter cette facture en retard des intérêts ? Les intérêts seront ramenés à 0,00 $.`
            : $localize`:@@bill.exemption.remove:Retirer l'exemption d'intérêts et recalculer les intérêts?`,
          confirmLabel: $localize`:@@bill.confirm:Confirmer`,
          cancelLabel: $localize`:@@bill.cancel:Annuler`,
        },
      })
      .afterClosed()
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.applyInterestExemption(current, exempt);
      });
  }

  private applyInterestExemption(current: BillRecord, exempt: boolean): void {
    this.updatingInterest.set(true);
    this.interestError.set(null);
    this.interestSuccess.set(null);

    let exemptionSaved = false;

    this.billService
      .setInterestExempt(current.billId, exempt)
      .pipe(
        switchMap(() => {
          exemptionSaved = true;
          return this.billService.getBillById(current.billId);
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.updatingInterest.set(false);
        }),
      )
      .subscribe({
        next: (updatedBill: BillRecord) => {
          // Refresh the complete bill, including interest and total.
          this.bill.set(updatedBill);

          this.interestSuccess.set(
            exempt ? 'Interest exemption applied.' : 'Interest exemption removed.',
          );
        },
        error: () => {
          this.interestError.set(
            exemptionSaved
              ? 'The exemption was saved, but the bill could not be refreshed. Reopen the bill to see updated totals.'
              : 'Unable to update interest exemption. Please try again.',
          );
        },
      });
  }

  protected close(): void {
    this.dialogRef.close(this.bill());
  }
}
