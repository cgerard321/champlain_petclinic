import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthState } from '@core/services/auth-state';
import { Roles } from '@shared/models/roles';

import { CustomerResponseModel } from '@features/cust/models/customerResponseModel';
import { CustomerService } from '@features/cust/services/customer-service';

@Component({
  selector: 'app-customer-details',
  imports: [ReactiveFormsModule],
  templateUrl: './customerDetails.html',
  styleUrl: './customerDetails.css',
})
export class CustomerDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly customerService = inject(CustomerService);
  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    address: ['', Validators.required],
    city: ['', Validators.required],
    province: ['', Validators.required],
    telephone: ['', Validators.required],
  });
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

    this.customerService.getCustomer(customerId).subscribe({
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
  }
  protected handleEditCustomer(): void {
    const customer = this.customer();
    if (!customer) return;
    this.form.reset({ firstName: customer.firstName, lastName: customer.lastName, address: customer.address, city: customer.city, province: customer.province, telephone: customer.telephone });
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
          this.customer.set(updatedCustomer);
          this.isEditing.set(false);
          this.isSaving.set(false);
        },
        error: () => {
          this.errorMessage.set('Could not update customer. Please try again.');
          this.isSaving.set(false);
        },
      });
  }
}
