import { Component, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { form, FormField, required } from '@angular/forms/signals';
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
import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthState } from '@core/services/auth-state';
import { BillRequestModel } from '@features/bill/models/bill.model';
import { BillService } from '@features/bill/services/bill-service';
import { Customer } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';
import { Veterinarian } from '@features/vets/models/vet.model';
import { VetService } from '@features/vets/services/vet-service';

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
    FormField,
  ],
  providers: [provideNativeDateAdapter()],
  selector: 'app-create-bill',
  styleUrl: './create-bill.css',
  templateUrl: './create-bill.html',
})
export class CreateBill implements OnInit {
  protected auth = inject(AuthState);
  protected billService = inject(BillService);
  protected customerService = inject(CustomerService);
  protected vetService = inject(VetService);

  private readonly untilDestroyed = takeUntilDestroyed<Customer>();
  private readonly untilDestroyedVet = takeUntilDestroyed<Veterinarian>();
  protected readonly customers = signal<Customer[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly vets = signal<Veterinarian[]>([]);

  private today = new Date();

  private newDate: Date = (() => {
    const date = new Date(this.today);
    date.setDate(date.getDate() + 30);
    return this.newDate;
  })();

  protected readonly billRequest = signal<BillRequestModel>({
    customerId: '',
    visitType: '',
    vetId: '',
    date: this.today,
    amount: null,
    billStatus: 'UNPAID',
    dueDate: this.newDate,
  });

  private _snackBar = inject(MatSnackBar);

  protected showSnackbar(message: string): void {
    this._snackBar.open(message, 'Close', {
      duration: 3000,
    });
  }

  protected readonly billRequestForm = form(this.billRequest, (schemaPath) => {
    required(schemaPath.customerId, { message: 'Customer is required' });
    required(schemaPath.visitType, { message: 'Visit Type is required' });
    required(schemaPath.vetId, { message: 'Vet is required' });
    required(schemaPath.date, { message: 'Date is required' });
    required(schemaPath.amount, { message: 'Amount is required' });
    required(schemaPath.billStatus, { message: 'Bill status is required' });
  });

  readonly dialogRef = inject(MatDialogRef<CreateBill>);

  closeCreateBill(): void {
    this.dialogRef.close();
  }

  protected readonly submitted = signal(false);

  onSubmit(event: Event): void {
    event.preventDefault();
    this.submitted.set(true);

    if (this.billRequestForm().invalid()) {
      return;
    }
    this.billService.createBill(this.billRequest()).subscribe({
      next: (createdBill: BillRequestModel) => {
        this.dialogRef.close(createdBill);
        this.showSnackbar('Bill created successfully!');
      },
      error: (error) => {
        console.warn('Error status:', error.status);
        console.warn('Error body:', error.error);
      },
    });
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

    const userId = this.auth.userId();

    this.vetService
      .getVetByVetId(userId)
      .pipe(this.untilDestroyedVet)
      .subscribe({
        next: (veterinarian) => {
          this.vets.set([veterinarian]);
          this.billRequest.update((request) => ({
            ...request,
            vetId: veterinarian.vetId,
          }));
        },
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set('Unable to load veterinarians. Please try again later.');
        },
        complete: () => this.isLoading.set(false),
      });
  }
}
