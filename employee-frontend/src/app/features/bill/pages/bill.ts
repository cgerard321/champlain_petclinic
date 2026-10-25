import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, finalize, switchMap, take, tap, toArray } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { ArchiveBillsDialog } from '@features/bill/components/archive-bills-dialog/archive-bills-dialog';
import { BillDetails } from '@features/bill/components/bill-details/bill-details';
import { BillUpdate } from '@features/bill/components/bill-update/bill-update';
import { CreateBill } from '@features/bill/components/createBill/create-bill';
import { BillRecord } from '@features/bill/models/bill.model';
import { BillService } from '@features/bill/services/bill-service';
import { Roles } from '@shared/models/roles';

interface BillFilters {
  customerId: string;
  customerName: string;
  vetName: string;
  visitType: string;
  status: string;
  year: string;
  month: string;
}

interface BillSearchSuggestion {
  label: string;
  type: 'customer' | 'veterinarian';
}

const emptyFilters: BillFilters = {
  customerId: '',
  customerName: '',
  vetName: '',
  visitType: '',
  status: '',
  year: '',
  month: '',
};

@Component({
  selector: 'app-bill',
  imports: [CurrencyPipe, FormsModule],
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

  protected readonly currency = signal<'CAD' | 'USD'>('CAD');
  protected readonly filters = signal<BillFilters>({ ...emptyFilters });
  protected readonly billSearchInput = signal('');
  protected readonly billSearchQuery = signal('');
  protected readonly showBillSearchSuggestions = signal(false);

  protected readonly updatingInterestId = signal<string | null>(null);
  protected readonly downloadingPdfId = signal<string | null>(null);
  protected readonly billActionError = signal<string | null>(null);
  protected readonly billActionSuccess = signal<string | null>(null);

  protected toggleInterestExemption(bill: BillRecord): void {
    if (!this.canViewBills() || this.updatingInterestId() !== null) {
      return;
    }

    const exempt = !bill.interestExempt;
    let exemptionSaved = false;

    this.updatingInterestId.set(bill.billId);
    this.billActionError.set(null);
    this.billActionSuccess.set(null);

    this.billService
      .setInterestExempt(bill.billId, exempt)
      .pipe(
        tap(() => {
          exemptionSaved = true;
        }),
        switchMap(() => this.billService.getBillById(bill.billId)),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.updatingInterestId.set(null)),
      )
      .subscribe({
        next: (updatedBill) => {
          this.bills.update((currentBills) =>
            currentBills.map((item) => (item.billId === updatedBill.billId ? updatedBill : item)),
          );

          this.billActionSuccess.set(
            exempt ? 'Interest exemption applied.' : 'Interest exemption removed.',
          );
        },
        error: () => {
          this.billActionError.set(
            exemptionSaved
              ? 'The exemption was saved, but the bill could not be refreshed. Reload the bill list.'
              : 'Unable to update interest exemption. Please try again.',
          );
        },
      });
  }

  protected downloadPdf(bill: BillRecord): void {
    if (!this.canViewDetails() || this.downloadingPdfId() !== null) {
      return;
    }

    const currency = this.currency();

    this.downloadingPdfId.set(bill.billId);
    this.billActionError.set(null);
    this.billActionSuccess.set(null);

    this.billService
      .downloadBillPdf(bill.billId, currency)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.downloadingPdfId.set(null)),
      )
      .subscribe({
        next: (pdf) => {
          const url = URL.createObjectURL(pdf);
          const link = document.createElement('a');

          link.href = url;
          link.download = `bill-${bill.billId}-${currency}.pdf`;
          document.body.appendChild(link);

          try {
            link.click();
          } finally {
            link.remove();
            setTimeout(() => URL.revokeObjectURL(url), 1000);
          }

          this.billActionSuccess.set('Bill PDF download started.');
        },
        error: () => {
          this.billActionError.set('Unable to download the PDF. Please try again.');
        },
      });
  }

  protected readonly visitTypes = computed(() =>
    [...new Set(this.bills().map((bill) => bill.visitType))]
      .filter((visitType) => visitType.trim().length > 0)
      .sort((a, b) => a.localeCompare(b)),
  );

  protected updateFilter(key: keyof BillFilters, value: string): void {
    this.filters.update((current) => ({
      ...current,
      [key]: value,
    }));
  }

  protected clearFilters(): void {
    this.filters.set({ ...emptyFilters });
  }

  protected updateBillSearch(value: string): void {
    this.billSearchInput.set(value);

    if (!value.trim()) {
      this.billSearchQuery.set('');
      this.showBillSearchSuggestions.set(false);
      return;
    }

    this.showBillSearchSuggestions.set(true);
  }

  protected submitBillSearch(event: Event): void {
    event.preventDefault();
    this.billSearchQuery.set(this.billSearchInput().trim());
    this.showBillSearchSuggestions.set(false);
  }

  protected selectBillSearchSuggestion(suggestion: BillSearchSuggestion): void {
    this.billSearchInput.set(suggestion.label);
    this.billSearchQuery.set(suggestion.label);
    this.showBillSearchSuggestions.set(false);
  }

  protected clearBillSearch(): void {
    this.billSearchInput.set('');
    this.billSearchQuery.set('');
    this.showBillSearchSuggestions.set(false);
  }

  protected readonly billSearchSuggestions = computed<BillSearchSuggestion[]>(() => {
    const search = this.billSearchInput().trim().toLowerCase();

    if (search.length < 3) {
      return [];
    }

    const activeArchive = this.showArchivedBills();
    const suggestions = new Map<string, BillSearchSuggestion>();

    for (const bill of this.bills()) {
      if (this.canViewBills() && (bill.archive === true) !== activeArchive) {
        continue;
      }

      const names: Array<BillSearchSuggestion> = [
        {
          label: `${bill.customerFirstName ?? ''} ${bill.customerLastName ?? ''}`.trim(),
          type: 'customer',
        },
        {
          label: `${bill.vetFirstName ?? ''} ${bill.vetLastName ?? ''}`.trim(),
          type: 'veterinarian',
        },
      ];

      for (const suggestion of names) {
        const key = `${suggestion.type}:${suggestion.label.toLowerCase()}`;

        if (suggestion.label && suggestion.label.toLowerCase().includes(search)) {
          suggestions.set(key, suggestion);
        }
      }
    }

    return [...suggestions.values()].sort((a, b) => a.label.localeCompare(b.label));
  });

  protected setCurrency(value: string): void {
    if (value === 'CAD' || value === 'USD') {
      this.currency.set(value);
    }
  }

  protected convertAmount(amount: number): number {
    // Matches the existing backend PDF conversion rate.
    return this.currency() === 'USD' ? amount * 0.73 : amount;
  }

  protected readonly canCreateBill = computed(() => this.auth.hasRole(Roles.vet));

  protected readonly canViewDetails = computed(
    () => this.auth.hasRole(Roles.admin) || this.auth.hasRole(Roles.vet),
  );
  protected openCreateBill(): void {
    if (!this.canCreateBill()) {
      return;
    }

    this.dialog
      .open(CreateBill)
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
    const filters = this.filters();
    const searchQuery = this.billSearchQuery();

    const matchesText = (value: string | null, search: string): boolean =>
      (value ?? '').toLowerCase().includes(search.trim().toLowerCase());

    return this.bills().filter((bill) => {
      const matchesArchive = !this.canViewBills()
        ? true
        : this.showArchivedBills()
          ? bill.archive === true
          : bill.archive !== true;

      const customerName = `${bill.customerFirstName ?? ''} ${bill.customerLastName ?? ''}`.trim();

      const vetName = `${bill.vetFirstName ?? ''} ${bill.vetLastName ?? ''}`.trim();

      return (
        matchesArchive &&
        (matchesText(customerName, searchQuery) || matchesText(vetName, searchQuery)) &&
        (!this.canViewBills() ||
          (matchesText(bill.customerId, filters.customerId) &&
            matchesText(customerName, filters.customerName) &&
            matchesText(vetName, filters.vetName) &&
            (!filters.visitType || bill.visitType === filters.visitType) &&
            (!filters.status || bill.billStatus === filters.status) &&
            (!filters.year || bill.date.getFullYear() === Number(filters.year)) &&
            (!filters.month || bill.date.getMonth() + 1 === Number(filters.month))))
      );
    });
  });
}
