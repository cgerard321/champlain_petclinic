import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { OwnerApiService } from '../../api/owner-api.service';
import { Owner } from '../../models/owner.model';

@Component({
  selector: 'app-pet-owner-detail',
  standalone: true,
  imports: [CommonModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/ownerdetail.css" rel="stylesheet" type="text/css" />
    <link href="/css/customers/owner.css" rel="stylesheet" type="text/css" />

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">Pet Owner Details</h1>
          <p class="page-subtitle">View owner information and their pets</p>
        </div>

        <div class="content-wrapper">
          <div *ngIf="!owner" class="empty-state">
            <div class="empty-state-icon">&#x1F464;</div>
            <h3>Loading owner details...</h3>
          </div>

          <div *ngIf="owner" class="details-grid">
            <!-- Owner Information Card -->
            <div class="details-card">
              <div class="card-header">
                <h2 class="card-title">Owner Information</h2>
              </div>

              <div class="profile-section">
                <div class="profile-avatar">
                  <img src="../../images/default-user-icon.png" alt="Owner Avatar" />
                </div>
                <h3 class="profile-name">{{ owner.firstName }} {{ owner.lastName }}</h3>
              </div>

              <form class="owner-form">
                <div class="form-grid">
                  <div class="form-group full-width">
                    <input class="form-input" [value]="owner.ownerId" readonly disabled />
                    <label class="form-label">Owner ID</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner.firstName" readonly disabled />
                    <label class="form-label">First Name</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner.lastName" readonly disabled />
                    <label class="form-label">Last Name</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner.address" readonly disabled />
                    <label class="form-label">Address</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner.city" readonly disabled />
                    <label class="form-label">City</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner.province" readonly disabled />
                    <label class="form-label">Province</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner.telephone" readonly disabled />
                    <label class="form-label">Phone Number</label>
                  </div>
                </div>
              </form>
            </div>

            <!-- Pets Card -->
            <div class="details-card">
              <div class="card-header">
                <h2 class="card-title pets-title">Pets</h2>
              </div>

              <div class="pets-section">
                <div class="pets-list" *ngIf="owner.pets && owner.pets.length > 0">
                  <div *ngFor="let pet of owner.pets" class="pet-item">
                    <div class="pet-avatar">
                      <img src="../../images/animaldefault.png" alt="Pet Avatar" />
                    </div>
                    <div class="pet-info">
                      <h4 class="pet-name">{{ pet.name }}</h4>
                      <p class="pet-type">{{ getPetTypeName(pet.petTypeId) }}</p>
                      <p class="pet-birthday">{{ formatBirthday(pet.birthDate) }}</p>
                    </div>
                  </div>
                </div>

                <div *ngIf="!owner.pets || owner.pets.length === 0" class="empty-state">
                  <div class="empty-state-icon">&#x1F43E;</div>
                  <p class="empty-state-text">No pets registered for this owner</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class PetOwnerDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private ownerApi = inject(OwnerApiService);

  owner: Owner | null = null;
  ownerId: string = '';

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.ownerId = params['ownerId'] || '';
      if (this.ownerId) {
        this.loadOwnerData();
      }
    });
  }

  loadOwnerData(): void {
    this.ownerApi.getOwnerById(this.ownerId).subscribe({
      next: owner => {
        this.owner = owner;
      },
      error: () => {},
    });
  }

  getPetTypeName(petTypeId: string): string {
    const petTypes: { [key: string]: string } = {
      '1': 'Cat',
      '2': 'Dog',
      '3': 'Lizard',
      '4': 'Snake',
      '5': 'Bird',
      '6': 'Hamster',
    };
    return petTypes[petTypeId] || 'Unknown';
  }

  formatBirthday(birthday: string): string {
    if (!birthday) return '';

    const date = new Date(birthday);
    const timezoneOffset = date.getTimezoneOffset() * 60000;
    const adjustedDate = new Date(date.getTime() - timezoneOffset);

    const year = adjustedDate.getFullYear();
    const month = (adjustedDate.getMonth() + 1).toString().padStart(2, '0');
    const day = adjustedDate.getDate().toString().padStart(2, '0');

    return `${year} / ${month} / ${day}`;
  }
}
