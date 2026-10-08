import { Component, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { form, FormField } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { provideNativeDateAdapter, MatOption } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelect } from '@angular/material/select';

import { AuthState } from '@core/services/auth-state';
import { BillRequestModel } from '@features/bill/models/bill.model';
import { BillService } from '@features/bill/services/bill-service';
import { Customer } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';

@Component({
  imports: [
    MatFormFieldModule,
    MatCardModule,
    MatDatepickerModule,
    MatInputModule,
    MatSelect,
    MatOption,
    MatDividerModule,
    MatListModule,
    MatButtonModule,
],
  providers: [provideNativeDateAdapter()],
  selector: 'app-create-bill',
  styleUrl: './create-bill.css',
  templateUrl: './create-bill.html',
})
export class CreateBill implements OnInit{
  protected auth = inject(AuthState);
  protected billService = inject(BillService);
  protected customerService = inject(CustomerService);

  private readonly untilDestroyed = takeUntilDestroyed<Customer>();
  protected readonly customers = signal<Customer[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  readonly billRequest = signal<BillRequestModel>({
    customerId: '',
    visitType: '',
    vetId: '',
    date: '',
    amount: 0,
    billStatus: '',
  })

  readonly billRequestForm = form(this.billRequest);

  readonly dialogRef = inject(MatDialogRef<CreateBill>);

  closeCreateBill(): void {
    this.dialogRef.close();
  }

  ngOnInit(): void {
    this.customerService
      .getCustomers()
      .pipe(this.untilDestroyed)
      .subscribe({
        next: (customer) => this.customers.update((customers) => [...customers, customer]),
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set('Unable to load customers. Please try again later.');
        },
        complete: () => this.isLoading.set(false),
      });
  }
}
