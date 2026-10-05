import {Component,inject, OnInit, signal} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

import { Pet } from '@features/cust/models/customer.model'
import { CustomerService } from '@features/cust/services/customer-service';
import {MatProgressSpinner} from '@angular/material/progress-spinner';

interface CustomerPetDetails{
  customerId : string
}

@Component({
  selector: 'app-customer-pet-details',
  imports: [MatDialogModule, MatProgressSpinner, MatIconModule],
  templateUrl: './customer-pet-details.html',
  styleUrl: './customer-pet-details.css',
})
export class PetDetails implements OnInit {
  private readonly data = inject<CustomerPetDetails>(MAT_DIALOG_DATA);
  private readonly customerService = inject(CustomerService);

  protected readonly pets = signal<Pet[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.customerService.getPetDetails(this.data.customerId ).subscribe({
      next: (pet) => {
        this.pets.update((pets) => [...pets, pet]);
      },
      complete: () => {
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Pet details error:', error);
        console.log('Customer ID:', this.data.customerId);

        this.isLoading.set(false);
        this.errorMessage.set('Could not load pet details.');
      },
    });
  }

}
