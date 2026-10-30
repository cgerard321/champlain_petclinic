import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, toArray } from 'rxjs';

import { BillRecord } from '@features/bill/models/bill.model';
import { Customer } from '@features/bill/models/customer.model';
import { BillService } from '@features/bill/services/bill-service';
import { translateBillStatus } from '@features/bill/utils/bill-translations';

@Component({
  selector: 'app-customer-bill-lookup',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './customer-bill-lookup.html',
  styleUrls: ['../../pages/bill.css', './customer-bill-lookup.css'],
})
export class CustomerBillLookup implements OnInit {
  private readonly billService = inject(BillService);
  private readonly destroyRef = inject(DestroyRef);
  private billRequest?: Subscription;

  protected readonly customers = signal<Customer[]>([]);
  protected readonly bills = signal<BillRecord[]>([]);
  protected readonly selectedCustomerId = signal('');

  protected readonly translateStatus = translateBillStatus;

  protected readonly customersLoading = signal(true);
  protected readonly customersFailed = signal(false);
  protected readonly billsLoading = signal(false);
  protected readonly billsFailed = signal(false);

  ngOnInit(): void {
    this.loadCustomers();
  }

  protected loadCustomers(): void {
    this.customersLoading.set(true);
    this.customersFailed.set(false);

    this.billService
      .getCustomers()
      .pipe(toArray(), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (customers) => {
          this.customers.set(
            [...customers].sort((a, b) =>
              `${a.firstName} ${a.lastName}`.localeCompare(`${b.firstName} ${b.lastName}`),
            ),
          );
          this.customersLoading.set(false);
        },
        error: () => {
          this.customers.set([]);
          this.customersFailed.set(true);
          this.customersLoading.set(false);
        },
      });
  }

  protected selectCustomer(customerId: string): void {
    this.billRequest?.unsubscribe();

    this.selectedCustomerId.set(customerId);
    this.bills.set([]);
    this.billsFailed.set(false);
    this.billsLoading.set(false);

    if (!customerId) {
      return;
    }

    this.billsLoading.set(true);

    this.billRequest = this.billService
      .getBillsByCustomerId(customerId)
      .pipe(toArray(), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (bills) => {
          this.bills.set(bills);
          this.billsLoading.set(false);
        },
        error: () => {
          this.bills.set([]);
          this.billsFailed.set(true);
          this.billsLoading.set(false);
        },
      });
  }

  protected statusLabel(status: BillRecord['billStatus']): string {
    switch (status) {
      case 'PAID':
        return 'PAID';
      case 'UNPAID':
        return 'UNPAID';
      case 'OVERDUE':
        return 'OVERDUE';
    }
  }
}
