import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { OwnerApiService } from '../../api/owner-api.service';
import { Owner } from '../../models/owner.model';

@Component({
  selector: 'app-owner-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/owner.css" rel="stylesheet" type="text/css" />

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">{{ !isLoading && isEditMode ? 'Edit Owner' : 'Add Owner' }}</h1>
          <p class="page-subtitle">
            {{ !isLoading && isEditMode ? 'Update owner information' : 'Register a new owner' }}
          </p>
        </div>

        <div class="content-wrapper">
          <div *ngIf="isLoading" class="loading-state">
            <p>Loading owner data...</p>
          </div>

          <ng-container *ngIf="!isLoading && owner">
            <div class="form-card">
              <div class="form-header">
                <h2 class="form-title">Owner Information</h2>
                <p class="form-subtitle">
                  {{
                    isEditMode
                      ? "Update the owner's personal details"
                      : "Enter the owner's personal details"
                  }}
                </p>
              </div>

              <div *ngIf="checked" class="warning-section">
                <div class="warning-icon">&#x26A0;</div>
                <p class="warning-text">This owner is currently disabled and cannot be edited.</p>
              </div>

              <form
                id="ownerForm"
                class="owner-form"
                enctype="multipart/form-data"
                #ownerForm="ngForm"
              >
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
                      >First name is required.</span
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
                    [disabled]="isLoading || !isFormValid()"
                  >
                    {{ isEditMode ? 'Update Owner' : 'Create Owner' }}
                  </button>
                </div>

                <div class="form-help">
                  <p class="form-help-text">
                    Make sure all information is accurate before submitting
                  </p>
                </div>
              </form>
            </div>
          </ng-container>
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class OwnerFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ownerApi = inject(OwnerApiService);

  owner: Owner = {
    ownerId: '',
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  };

  ownerId: string = '';
  method: string = '';
  checked: boolean = false;
  isEditMode: boolean = false;
  isLoading: boolean = true;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.ownerId = params['ownerId'] || '';
      this.method = params['method'] || '';

      // Determine method from route path if not provided as parameter
      if (!this.method && this.ownerId) {
        const url = this.router.url;
        if (url.includes('/edit')) {
          this.method = 'edit';
        } else if (url.includes('/delete')) {
          this.method = 'delete';
        }
      }

      if (!this.ownerId) {
        this.checked = false;
        this.isEditMode = false;
        this.isLoading = false;
      } else {
        // Ensure loading state is true when starting API call
        this.isLoading = true;
        this.ownerApi.getOwnerById(this.ownerId).subscribe({
          next: owner => {
            if (owner) {
              this.owner.ownerId = owner.ownerId || '';
              this.owner.firstName = owner.firstName || '';
              this.owner.lastName = owner.lastName || '';
              this.owner.address = owner.address || '';
              this.owner.city = owner.city || '';
              this.owner.province = owner.province || '';
              this.owner.telephone = owner.telephone || '';
              this.isEditMode = true;
              this.isLoading = false;
              if (this.method === 'edit') {
                this.checked = false;
              } else {
                this.checked = true;
              }
            } else {
              this.isLoading = false;
            }
          },
          error: () => {
            this.isLoading = false;
          },
        });
      }
    });
  }

  isFormValid(): boolean {
    if (!this.owner) return false;
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

    if (this.ownerId) {
      if (this.method === 'edit') {
        this.ownerApi.updateOwner(this.ownerId, this.owner).subscribe({
          next: () => {
            alert('Owner updated successfully!');
            this.router.navigate(['/owners']);
          },
          error: () => {
            this.handleError(_error);
          },
        });
      } else {
        this.ownerApi.deleteOwner(this.ownerId).subscribe({
          next: () => {
            alert('Owner deleted successfully!');
            this.router.navigate(['/owners']);
          },
          error: () => {
            this.handleError(_error);
          },
        });
      }
    } else {
      this.ownerApi.createOwner(this.owner).subscribe({
        next: () => {
          alert('Owner created successfully!');
          this.router.navigate(['/owners']);
        },
        error: () => {
          this.handleError(_error);
        },
      });
    }
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
