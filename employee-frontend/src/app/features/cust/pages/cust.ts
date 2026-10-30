import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink, Router } from '@angular/router';

import { AuthState } from '@core/services/auth-state';
import { AddCustomer } from '@features/cust/components/customer-add/customer-add';
import { PetDetails } from '@features/cust/components/customer-pet-details/customer-pet-details';
import { Customer } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [MatProgressSpinnerModule, MatCardModule, MatDialogModule, RouterLink],
  selector: 'app-cust',
  styleUrl: './cust.css',
  templateUrl: './cust.html',
})
export class Cust implements OnInit {
  private readonly router = inject(Router);
  private readonly customerService = inject(CustomerService);
  private readonly dialog = inject(MatDialog);
  private readonly auth = inject(AuthState);
  private readonly untilDestroyed = takeUntilDestroyed<Customer>();
  protected readonly petDetails = signal<PetDetails[]>([]);
  protected readonly customers = signal<Customer[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly canViewCustomerDetails = computed(
    () => this.auth.hasRole(Roles.admin) || this.auth.hasRole(Roles.receptionist),
  );
  protected readonly notAvailable = $localize`:@@cust.notAvailable:Non disponible`;
  protected readonly searchPlaceholder = $localize`:@@cust.searchPlaceholder:Rechercher un client par nom, prénom, ID, téléphone, adresse ou ville`;
  protected readonly isAdmin = computed(() => this.auth.hasRole(Roles.admin));
  protected readonly showDeactivated = signal(false);
  protected readonly deactivatedCustomers = signal<Customer[]>([]);
  protected readonly deactivatedError = signal<string | null>(null);
  protected readonly deactivatedLoadError = $localize`:@@cust.deactivated.loadError:Impossible de charger les comptes désactivés.`;

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

  protected checkPetDetails(customerId: string): void {
    this.dialog.open(PetDetails, {
      data: {
        customerId,
      },
      autoFocus: 'first-tabbable',
      width: '850px',
      maxWidth: '95vw',
      maxHeight: '90vh',
    });
  }

  protected readonly searchText = signal('');

  protected readonly filteredCustomers = computed(() => {
    const query = this.searchText().trim().toLowerCase();

    return this.customers().filter((customer) =>
      [
        `${customer.firstName} ${customer.lastName}`,
        customer.customerId,
        customer.telephone,
        customer.address,
        customer.city,
      ].some((value) =>
        String(value ?? '')
          .toLowerCase()
          .includes(query),
      ),
    );
  });

  protected checkCustomerDetails(customerId: string): void {
    this.router.navigate(['/customerDetails', customerId]);
  }
  protected addCustomerPopUp(): void {
    this.dialog.open(AddCustomer, {
      width: '650px',
      maxWidth: '90vw',
    });
  }

  protected toggleDeactivated(): void {
    this.showDeactivated.update((shown) => !shown);
    if (!this.showDeactivated()) {
      return;
    }
    this.deactivatedCustomers.set([]);
    this.deactivatedError.set(null);
    this.customerService
      .getSoftDeletedCustomers()
      .pipe(this.untilDestroyed)
      .subscribe({
        next: (customer) => this.deactivatedCustomers.update((list) => [...list, customer]),
        error: () => this.deactivatedError.set(this.deactivatedLoadError),
      });
  }
}
