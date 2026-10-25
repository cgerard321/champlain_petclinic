import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

import { CustomerService } from '@features/cust/services/customer-service';

@Component({
  selector: 'app-customer-add',
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './customer-add.html',
  styleUrl: './customer-add.css',
})
export class AddCustomer {
  private dialogRef = inject(MatDialogRef<AddCustomer>);
  private service = inject(CustomerService);

  customerForm = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    address: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    city: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    province: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    telephone: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{10}$/)],
    }),
  });

  apiError = '';

  onSubmit(): void {
    // Check the form before submitting
    if (this.customerForm.invalid) {
      this.customerForm.markAllAsTouched();
      return;
    }

    // Get the values from the form
    const customer = this.customerForm.getRawValue();

    this.service.addCustomer(customer).subscribe({
      next: (createdCustomer) => {
        this.dialogRef.close(createdCustomer);
        window.location.reload();
      },
      error: () => {
        this.apiError = 'Unable to create customer.';
      },
    });
  }

  close(): void {
    this.dialogRef.close();
  }

  protected readonly provincesOfCanada = [
    'alberta',
    'britishColumbia',
    'manitoba',
    'newBrunswick',
    'newfoundlandAndLabrador',
    'northwestTerritories',
    'novaScotia',
    'nunavut',
    'ontario',
    'princeEdwardIsland',
    'quebec',
    'saskatchewan',
    'yukon',
  ];

  protected readonly provinceLabels: Record<string, string> = {
    alberta: $localize`:@@cust.provinces.alberta:Alberta`,
    britishColumbia: $localize`:@@cust.provinces.britishColumbia:Colombie-Britannique`,
    manitoba: $localize`:@@cust.provinces.manitoba:Manitoba`,
    newBrunswick: $localize`:@@cust.provinces.newBrunswick:Nouveau-Brunswick`,
    newfoundlandAndLabrador: $localize`:@@cust.provinces.newfoundlandAndLabrador:Terre-Neuve-et-Labrador`,
    northwestTerritories: $localize`:@@cust.provinces.northwestTerritories:Territoires du Nord-Ouest`,
    novaScotia: $localize`:@@cust.provinces.novaScotia:Nouvelle-Écosse`,
    nunavut: $localize`:@@cust.provinces.nunavut:Nunavut`,
    ontario: $localize`:@@cust.provinces.ontario:Ontario`,
    princeEdwardIsland: $localize`:@@cust.provinces.princeEdwardIsland:Île-du-Prince-Édouard`,
    quebec: $localize`:@@cust.provinces.quebec:Québec`,
    saskatchewan: $localize`:@@cust.provinces.saskatchewan:Saskatchewan`,
    yukon: $localize`:@@cust.provinces.yukon:Yukon`,
  };
}
