import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PetApiService } from '../../api/pet-api.service';
import { Pet } from '../../models/pet.model';

@Component({
  selector: 'app-pet-details',
  standalone: true,
  imports: [CommonModule, RouterModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/pet.css" rel="stylesheet" type="text/css"/>

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">Pet Details</h1>
          <p class="page-subtitle">View and manage pet information</p>
        </div>

        <div class="content-wrapper">
          <div *ngIf="!pet" class="empty-state">
            <div class="empty-state-icon">&#x1F43E;</div>
            <h3>No pet found</h3>
            <p>The requested pet could not be found.</p>
          </div>

          <div *ngIf="pet" class="details-card">
            <div class="card-header">
              <h2 class="card-title">Pet Information</h2>
              <div class="status-badge" [class.status-active]="pet.isActive" [class.status-inactive]="!pet.isActive">
                <span *ngIf="pet.isActive">&#x2713; Active</span>
                <span *ngIf="!pet.isActive">&#x26A0; Inactive</span>
              </div>
            </div>

            <div class="profile-section">
              <div class="profile-avatar">
                <img src="../../images/animaldefault.png" alt="Pet Avatar"/>
              </div>
              <h3 class="profile-name">{{pet.name}}</h3>
              <p class="profile-type">{{getPetTypeName(pet.petTypeId)}}</p>
            </div>

            <form class="pet-form">
              <div class="form-grid">
                <div class="form-group">
                  <input class="form-input" [value]="pet.petId" readonly disabled/>
                  <label class="form-label">Pet ID</label>
                </div>

                <div class="form-group">
                  <input class="form-input" [value]="pet.ownerId" readonly disabled/>
                  <label class="form-label">Owner ID</label>
                </div>

                <div class="form-group">
                  <input class="form-input" [value]="pet.name" readonly disabled/>
                  <label class="form-label">Name</label>
                </div>

                <div class="form-group">
                  <input class="form-input" [value]="getBirthday(pet.birthDate)" readonly disabled/>
                  <label class="form-label">Birthday</label>
                </div>

                <div class="form-group">
                  <input class="form-input" [value]="pet.weight" readonly disabled/>
                  <label class="form-label">Weight (KG)</label>
                </div>

                <div class="form-group">
                  <input class="form-input" [value]="getPetTypeName(pet.petTypeId)" readonly disabled/>
                  <label class="form-label">Pet Type</label>
                </div>

                  <div class="form-group full-width">
                    <input class="form-input" [value]="pet.isActive ? 'Active' : 'Inactive'" readonly disabled/>
                    <label class="form-label">Status</label>
                  </div>
                </div>
              </form>

              <div class="action-buttons">
                <a class="btn-submit" [routerLink]="['/owners', pet.ownerId, 'pets', pet.petId, 'edit']">
                  &#x270F; Edit Pet
                </a>
                <button class="btn-modern btn-status" 
                        [class.inactive]="pet.isActive"
                        (click)="toggleActiveStatus(pet.petId)">
                  <span *ngIf="pet.isActive">Deactivate</span>
                  <span *ngIf="!pet.isActive">Activate</span>
                </button>
                <button class="btn-modern btn-delete" (click)="deletePet(pet.petId)">
                  &#x1F5D1; Delete Pet
                </button>
              </div>

              <div class="info-card">
                <p class="info-text">Use the buttons above to manage this pet's status and information</p>
              </div>
            </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class PetDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private petApi = inject(PetApiService);

  pet: Pet | null = null;
  petId: string = '';

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.petId = params['petId'];
      this.loadPetData();
    });
  }

  loadPetData(): void {
    this.petApi.getPetById(this.petId).subscribe({
      next: (pet) => {
        this.pet = pet;
      },
      error: (_error) => {
      }
    });
  }

  getPetTypeName(petTypeId: string): string {
    const petTypes: { [key: string]: string } = {
      '1': 'Cat',
      '2': 'Dog',
      '3': 'Lizard',
      '4': 'Snake',
      '5': 'Bird',
      '6': 'Hamster'
    };
    return petTypes[petTypeId] || 'Unknown';
  }

  getBirthday(birthday: string): string {
    if (!birthday) return '';
    
    const date = new Date(birthday);
    const timezoneOffset = date.getTimezoneOffset() * 60000;
    const adjustedDate = new Date(date.getTime() - timezoneOffset);
    
    const year = adjustedDate.getFullYear();
    const month = (adjustedDate.getMonth() + 1).toString().padStart(2, '0');
    const day = adjustedDate.getDate().toString().padStart(2, '0');
    
    return `${year} / ${month} / ${day}`;
  }

  toggleActiveStatus(petId: string): void {
    if (!this.pet) return;
    
    // Update the pet
    this.petApi.updatePet(petId, {
      name: this.pet.name,
      birthDate: this.pet.birthDate,
      petTypeId: this.pet.petTypeId,
      weight: this.pet.weight
    }).subscribe({
      next: (updatedPet) => {
        this.pet = updatedPet;
      },
      error: (_error) => {
        alert('Error updating pet status. Please try again.');
      }
    });
  }

  deletePet(petId: string): void {
    const confirmed = confirm('Are you sure you want to delete this pet?');
    if (confirmed && this.pet) {
      this.petApi.deletePet(petId).subscribe({
        next: () => {
          // Navigate back to owner details like Angular JS
          this.router.navigate(['/owners', this.pet!.ownerId]);
        },
        error: (_error) => {
        }
      });
    }
  }
}