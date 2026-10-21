import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { catchError, mergeMap, of } from 'rxjs';
import { Router } from '@angular/router';

import { Pet, PetType } from '@features/cust/models/customer.model';
import { CustomerResponseModel } from '@features/cust/models/customerResponseModel';
import { CustomerService } from '@features/cust/services/customer-service';
import {MatDialogClose} from '@angular/material/dialog';

@Component({
  selector: 'app-customer-details',
  imports: [DatePipe, ReactiveFormsModule, MatDialogClose],
  templateUrl: './customerDetails.html',
  styleUrl: './customerDetails.css',
})
export class CustomerDetails implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly customerService = inject(CustomerService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    address: ['', Validators.required],
    city: ['', Validators.required],
    province: ['', Validators.required],
    telephone: ['', Validators.required],
  });

  protected readonly pets = signal<Pet[]>([]);
  protected readonly petTypes = signal<PetType[]>([]);
  protected readonly isLoadingPets = signal(true);
  protected readonly petErrorMessage = signal<string | null>(null);
  protected readonly petTypesErrorMessage = signal<string | null>(null);
  protected readonly isSaving = signal(false);
  protected readonly isEditing = signal(false);
  protected readonly customer = signal<CustomerResponseModel | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly provinces = [
    'Alberta',
    'British Columbia',
    'Manitoba',
    'New Brunswick',
    'Newfoundland and Labrador',
    'Nova Scotia',
    'Ontario',
    'Prince Edward Island',
    'Quebec',
    'Saskatchewan',
    'Northwest Territories',
    'Nunavut',
    'Yukon',
  ];

  ngOnInit(): void {
    const customerId = this.route.snapshot.paramMap.get('customerId');
    if (!customerId) {
      this.isLoading.set(false);
      this.errorMessage.set('No customer ID provided.');
      return;
    }

    this.customerService.getCustomer(customerId, true).subscribe({
      next: (customer) => {
        this.customer.set(customer);
      },
      complete: () => {
        this.isLoading.set(false);
      },

      error: (error) => {
        console.error('Customer details error:', error);

        this.isLoading.set(false);
        this.errorMessage.set('Could not load customer details.');
      },
    });

    this.customerService
      .getPetDetails(customerId)
      .pipe(
        mergeMap(
          (pet) => this.customerService.getPetWithPhoto(pet.petId).pipe(catchError(() => of(pet))),
          4,
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (pet) => this.pets.update((pets) => [...pets, pet]),
        complete: () => this.isLoadingPets.set(false),
        error: () => {
          this.isLoadingPets.set(false);
          this.petErrorMessage.set('Could not load pets. Please try again later.');
        },
      });

    this.customerService
      .getPetTypes()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (petType) => this.petTypes.update((types) => [...types, petType]),
        error: () => this.petTypesErrorMessage.set('Could not load pet types.'),
      });
  }

  protected getPetTypeName(petTypeId: string): string {
    return this.petTypes().find((type) => type.petTypeId === petTypeId)?.name ?? 'Unavailable';
  }
  protected handleEditCustomer(): void {
    const customer = this.customer();
    if (!customer) return;
    this.form.reset({
      firstName: customer.firstName,
      lastName: customer.lastName,
      address: customer.address,
      city: customer.city,
      province: customer.province,
      telephone: customer.telephone,
    });
    this.errorMessage.set(null);
    this.isEditing.set(true);
  }
  protected handleCustomerUpdated(): void {
    const customer = this.customer();
    if (!customer || this.isSaving()) return;
    const firstName = this.form.getRawValue().firstName.trim();
    const lastName = this.form.getRawValue().lastName.trim();
    const address = this.form.getRawValue().address.trim();
    const city = this.form.getRawValue().city.trim();
    const province = this.form.getRawValue().province.trim();
    const telephone = this.form.getRawValue().telephone.trim();
    this.form.controls.address.setValue(address);
    this.form.controls.city.setValue(city);
    this.form.controls.province.setValue(province);
    this.form.controls.telephone.setValue(telephone);
    this.form.controls.firstName.setValue(firstName);
    this.form.controls.lastName.setValue(lastName);
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.customerService
      .updateCustomer(customer.customerId, {
        firstName,
        lastName,
        address,
        city,
        province,
        telephone,
      })
      .subscribe({
        next: (updatedCustomer) => {
          this.customer.set({ ...updatedCustomer, photo: updatedCustomer.photo ?? customer.photo });
          this.isEditing.set(false);
          this.isSaving.set(false);
        },
        error: () => {
          this.errorMessage.set('Could not update customer. Please try again.');
          this.isSaving.set(false);
        },
      });
  }
  protected backToList(){
    this.router.navigate(['/cust']);
  }
}
