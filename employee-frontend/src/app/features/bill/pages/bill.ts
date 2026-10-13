import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { CreateBill } from '@features/bill/components/createBill/create-bill';
import { Subscription, finalize, switchMap, take, tap, toArray } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { ArchiveBillsDialog } from '@features/bill/components/archive-bills-dialog/archive-bills-dialog';
import { BillDetails } from '@features/bill/components/bill-details/bill-details';
import { BillUpdate } from '@features/bill/components/bill-update/bill-update';
import { BillRecord } from '@features/bill/models/bill.model';
import { BillService } from '@features/bill/services/bill-service';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-bill',
  imports: [CurrencyPipe],
  styleUrl: './bill.css',
  templateUrl: './bill.html',
})
export class Bill implements OnInit {
  protected readonly auth = inject(AuthState);
  protected readonly dialog = inject(MatDialog);

  private readonly billService = inject(BillService);
  private readonly destroyRef = inject(DestroyRef);
  private detailsRequest?: Subscription;

  protected readonly bills = signal<BillRecord[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly billIdInput = signal('');
  protected readonly isDetailsLoading = signal(false);
  protected readonly detailsError = signal<string | null>(null);

  protected readonly canViewBills = computed(() => this.auth.hasRole(Roles.admin));

  protected readonly canViewOwnBills = computed(() => this.auth.hasRole(Roles.vet));

  protected readonly canViewBillList = computed(
    () => this.canViewBills() || this.canViewOwnBills(),
  );

  protected readonly canCreateBill = computed(() => this.auth.hasRole(Roles.vet));

  protected readonly canViewDetails = computed(
    () => this.auth.hasRole(Roles.admin) || this.auth.hasRole(Roles.vet),
  );
  protected openCreateBill(): void {
    if (!this.canCreateBill()) {
      return;
    }

    this.dialog
      .open(CreateBill, {
        width: '750px',
        maxWidth: '95vw',
        maxHeight: '90vh',
      })
      .afterClosed()
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe((createdBill: unknown) => {
        if (createdBill) {
          this.loadBills();
        }
      });
  }

  ngOnInit(): void {
    if (this.canViewBillList()) {
      this.loadBills();
    }
  }

  protected loadBills(): void {
    if (!this.canViewBillList()) {
      return;
    }

    this.bills.set([]);
    this.errorMessage.set(null);
    this.isLoading.set(true);

    const bills$ = this.canViewBills()
      ? this.billService.getAllBills()
      : this.billService.getMyBills();

    bills$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (bill: BillRecord) => {
        this.bills.update((currentBills) => [...currentBills, bill]);
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Unable to load bills. Please try again.');
      },
      complete: () => {
        this.isLoading.set(false);
      },
    });
  }

  protected searchForBill(event: Event): void {
    event.preventDefault();
    this.openBillDetails(this.billIdInput());
  }

  protected openBillDetails(billId: string): void {
    if (!this.canViewDetails()) {
      return;
    }

    const trimmedId = billId.trim();

    this.detailsRequest?.unsubscribe();
    this.detailsError.set(null);

    if (!trimmedId) {
      this.isDetailsLoading.set(false);
      this.detailsError.set('Please enter a Bill ID.');
      return;
    }

    this.isDetailsLoading.set(true);

    this.detailsRequest = this.billService
      .getBillById(trimmedId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (bill: BillRecord) => {
          this.isDetailsLoading.set(false);
          this.dialog.open(BillDetails, {
            data: bill,
            width: '780px',
            maxWidth: '95vw',
            maxHeight: '90vh',
          });
        },
        error: (error: unknown) => {
          this.isDetailsLoading.set(false);

          if (error instanceof HttpErrorResponse) {
            if (error.status === 404) {
              this.detailsError.set('Bill not found.');
              return;
            }

            if (error.status === 401 || error.status === 403) {
              this.detailsError.set('You do not have permission to view this bill.');
              return;
            }
          }

          this.detailsError.set('Unable to load bill details. Please try again.');
        },
        complete: () => {
          this.isDetailsLoading.set(false);
        },
      });
  }

  protected closeBillDetails(): void {
    this.detailsRequest?.unsubscribe();
    this.detailsError.set(null);
    this.isDetailsLoading.set(false);
  }

  protected openBillUpdate(bill: BillRecord): void {
    if (!this.auth.hasRole(Roles.admin)) {
      return;
    }

    this.dialog
      .open(BillUpdate, {
        data: { bill },
        width: '650px',
        maxWidth: '95vw',
        maxHeight: '90vh',
      })
      .afterClosed()
      .subscribe((updatedBill: BillRecord | undefined) => {
        if (updatedBill) {
          this.bills.update((existingBills) =>
            existingBills.map((item) => (item.billId === updatedBill.billId ? updatedBill : item)),
          );
        }
      });
  }

  private readonly archiveDestroyRef = inject(DestroyRef);

  protected readonly isArchiving = signal(false);
  protected readonly archiveSuccess = signal<string | null>(null);
  protected readonly archiveError = signal<string | null>(null);

  protected openArchiveBills(): void {
    if (!this.auth.hasRole(Roles.admin) || this.isArchiving()) {
      return;
    }

    this.dialog
      .open(ArchiveBillsDialog, {
        width: '500px',
        maxWidth: '95vw',
        autoFocus: 'first-tabbable',
      })
      .afterClosed()
      .pipe(take(1), takeUntilDestroyed(this.archiveDestroyRef))
      .subscribe((confirmed: boolean | undefined) => {
        if (confirmed !== true || this.isArchiving()) {
          return;
        }

        this.performArchive();
      });
  }

  private performArchive(): void {
    this.isArchiving.set(true);
    this.archiveSuccess.set(null);
    this.archiveError.set(null);

    let archiveCompleted = false;

    this.billService
      .archiveEligibleBills()
      .pipe(
        tap(() => {
          archiveCompleted = true;
        }),
        switchMap(() => this.billService.getAllBills().pipe(toArray())),
        finalize(() => this.isArchiving.set(false)),
        takeUntilDestroyed(this.archiveDestroyRef),
      )
      .subscribe({
        next: (refreshedBills: BillRecord[]) => {
          this.bills.set(refreshedBills);

          this.archiveSuccess.set(
            'Archive check completed. All eligible bills have been processed.',
          );
        },
        error: () => {
          this.archiveError.set(
            archiveCompleted
              ? 'The archive request succeeded, but the bill list could not be refreshed. Reload the page to check the results.'
              : 'Unable to archive bills. Please try again.',
          );
        },
      });
  }

  protected readonly showArchivedBills = signal(false);

  protected readonly displayedBills = computed(() => {
    if (!this.canViewBills()) {
      return this.bills();
    }

    return this.bills().filter((bill) =>
      this.showArchivedBills() ? bill.archive === true : bill.archive !== true,
    );
  });
}
