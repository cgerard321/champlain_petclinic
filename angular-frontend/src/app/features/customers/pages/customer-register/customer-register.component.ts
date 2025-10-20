import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { OwnerApiService } from '../../api/owner-api.service';
import { OwnerRequest } from '../../models/owner.model';

@Component({
  selector: 'app-customer-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/registration.css" rel="stylesheet" type="text/css" />

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">Register Owner</h1>
          <p class="page-subtitle">Create a new owner account</p>
        </div>

        <div class="content-wrapper">
          <div class="form-card">
            <form class="owner-form" enctype="multipart/form-data">
              <div class="form-grid">
                <div class="form-group">
                  <label class="form-label">First Name</label>
                  <input
                    class="form-input"
                    [(ngModel)]="owner.firstName"
                    name="firstName"
                    [disabled]="checked"
                    maxlength="50"
                    pattern="^[\\x20-\\x7F]+$"
                    required
                  />
                  <span *ngIf="ownerForm.firstName?.$error?.required" class="error-message"
                    >First Name is required.</span
                  >
                </div>

                <div class="form-group">
                  <label class="form-label">Last Name</label>
                  <input
                    class="form-input"
                    [(ngModel)]="owner.lastName"
                    name="lastName"
                    [disabled]="checked"
                    maxlength="50"
                    pattern="^[\\x20-\\x7F]+$"
                    required
                  />
                  <span *ngIf="ownerForm.lastName?.$error?.required" class="error-message"
                    >Last name is required.</span
                  >
                </div>

                <div class="form-group">
                  <label class="form-label">Address</label>
                  <input
                    class="form-input"
                    [(ngModel)]="owner.address"
                    name="address"
                    [disabled]="checked"
                    maxlength="50"
                    pattern="^[\\x20-\\x7F]+$"
                    required
                  />
                  <span *ngIf="ownerForm.address?.$error?.required" class="error-message"
                    >Address is required.</span
                  >
                </div>

                <div class="form-group">
                  <label class="form-label">City</label>
                  <input
                    class="form-input"
                    [(ngModel)]="owner.city"
                    name="city"
                    [disabled]="checked"
                    maxlength="50"
                    pattern="^[\\x20-\\x7F]+$"
                    required
                  />
                  <span *ngIf="ownerForm.city?.$error?.required" class="error-message"
                    >City is required.</span
                  >
                </div>

                <div class="form-group">
                  <label class="form-label">Province</label>
                  <input
                    class="form-input"
                    [(ngModel)]="owner.province"
                    name="province"
                    [disabled]="checked"
                    maxlength="50"
                    pattern="^[\\x20-\\x7F]+$"
                    required
                  />
                  <span *ngIf="ownerForm.province?.$error?.required" class="error-message"
                    >Province is required.</span
                  >
                </div>

                <div class="form-group">
                  <label class="form-label">Telephone</label>
                  <input
                    class="form-input"
                    [(ngModel)]="owner.telephone"
                    name="telephone"
                    [disabled]="checked"
                    maxlength="50"
                    pattern="^[\\x20-\\x7F]+$"
                    required
                  />
                  <span *ngIf="ownerForm.telephone?.$error?.required" class="error-message"
                    >Telephone is required.</span
                  >
                </div>
              </div>

              <div class="form-actions">
                <button
                  class="btn-submit"
                  type="button"
                  (click)="submitOwnerForm()"
                  [disabled]="!isFormValid()"
                >
                  Register Owner
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class CustomerRegisterComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ownerApi = inject(OwnerApiService);

  owner: OwnerRequest = {
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  };

  ownerId: string = '';
  checked: boolean = false;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.ownerId = params['ownerId'] || '';
      if (!this.ownerId) {
        this.checked = false;
      } else {
        this.ownerApi.getOwnerById(this.ownerId).subscribe({
          next: owner => {
            this.owner = {
              firstName: owner.firstName,
              lastName: owner.lastName,
              address: owner.address,
              city: owner.city,
              province: owner.province,
              telephone: owner.telephone,
            };
          },
          error: () => {},
        });
      }
    });
  }

  isFormValid(): boolean {
    return !!(
      this.owner.firstName &&
      this.owner.lastName &&
      this.owner.address &&
      this.owner.city &&
      this.owner.province &&
      this.owner.telephone
    );
  }

  submitOwnerForm(): void {
    if (!this.isFormValid()) {
      alert('Please fill in all required fields.');
      return;
    }

    this.ownerApi.createOwner(this.owner).subscribe({
      next: () => {
        alert('Owner registered successfully!');
        this.router.navigate(['/owners']);
      },
      error: _error => {
        this.handleError(_error);
      },
    });
  }

  private handleError(error: {
    error?: { errors?: Array<{ field: string; defaultMessage: string }>; error?: string };
  }): void {
    const errorData = error.error || {};
    const errors = errorData.errors || [];
    const errorMessage = errorData.error || 'An error occurred';

    const errorMessages = errors
      .map((e: { field: string; defaultMessage: string }) => `${e.field}: ${e.defaultMessage}`)
      .join('\n');

    alert(errorMessage + (errorMessages ? '\n' + errorMessages : ''));
  }
}
