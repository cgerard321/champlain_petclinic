import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card'




import { Customer } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';
import { PetDetails } from '@features/cust/components/customer-pet-details/customer-pet-details';

@Component({
  imports: [
    MatProgressSpinnerModule,
    MatCardModule,
    MatDialogModule
  ],
  selector: 'app-cust',
  styleUrl: './cust.css',
  templateUrl: './cust.html',
})
export class Cust implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly dialog = inject(MatDialog);
  private readonly untilDestroyed = takeUntilDestroyed<Customer>();
  protected  readonly petDetails = signal<PetDetails[]>([]);
  protected readonly customers = signal<Customer[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

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
  protected checkPetDetails(customerId: string): void{
    this.dialog.open(PetDetails, {
      data: {
        customerId ,
      },
      autoFocus: 'first-tabbable',
      width: '850px',
      maxWidth: '95vw',
      maxHeight: '90vh',
    })
  }
}
