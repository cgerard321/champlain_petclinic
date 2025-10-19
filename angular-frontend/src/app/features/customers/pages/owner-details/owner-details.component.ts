import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { OwnerApiService } from '../../api/owner-api.service';
import { PetApiService } from '../../api/pet-api.service';
import { Owner } from '../../models/owner.model';
import { Pet } from '../../models/pet.model';

@Component({
  selector: 'app-owner-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/owner.css" rel="stylesheet" type="text/css"/>

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">Owner Details</h1>
          <p class="page-subtitle">View and manage owner information and their pets</p>
        </div>

        <div class="content-wrapper">
          <div class="details-grid">
            <!-- Owner Information Card -->
            <div class="details-card">
              <div class="card-header">
                <h2 class="card-title">Owner Information</h2>
                <div class="action-buttons">
                  <a class="btn-modern btn-edit" [routerLink]="['/owners', owner?.ownerId, 'edit']">
                    &#x270F; Edit
                  </a>
                  <a class="btn-modern btn-delete" (click)="deleteOwner()">
                    &#x1F5D1; Delete
                  </a>
                </div>
              </div>

              <div class="profile-section">
                <div class="profile-avatar">
                  <img src="../../images/default-user-icon.png" alt="Owner Avatar"/>
                </div>
                <h3 class="profile-name">{{owner?.firstName}} {{owner?.lastName}}</h3>
              </div>

              <form class="owner-form">
                <div class="form-grid">
                  <div class="form-group full-width">
                    <input class="form-input" [value]="owner?.ownerId" readonly disabled/>
                    <label class="form-label">Owner ID</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner?.firstName" readonly disabled/>
                    <label class="form-label">First Name</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner?.lastName" readonly disabled/>
                    <label class="form-label">Last Name</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner?.address" readonly disabled/>
                    <label class="form-label">Address</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner?.city" readonly disabled/>
                    <label class="form-label">City</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner?.province" readonly disabled/>
                    <label class="form-label">Province</label>
                  </div>

                  <div class="form-group">
                    <input class="form-input" [value]="owner?.telephone" readonly disabled/>
                    <label class="form-label">Phone Number</label>
                  </div>
                </div>
              </form>
            </div>

            <!-- Pets Card -->
            <div class="details-card">
              <div class="card-header">
                <h2 class="card-title pets-title">Pets</h2>
                <a class="btn-add-pet" [routerLink]="['/owners', owner?.ownerId, 'pets', 'new']">
                  &#x2795; Add Pet
                </a>
              </div>

              <div class="pets-section">
                <div class="pets-list" *ngIf="pets && pets.length > 0">
                  <div *ngFor="let pet of pets" class="pet-item" [routerLink]="['/owners', owner?.ownerId, 'pets', pet.petId]">
                    <div class="pet-avatar">
                      <img src="../../images/animaldefault.png" alt="Pet Avatar"/>
                    </div>
                    <div class="pet-info">
                      <h4 class="pet-name">{{pet.name}}</h4>
                      <p class="pet-type">{{getPetTypeName(pet.petTypeId)}}</p>
                      <p class="pet-birthday">{{formatBirthday(pet.birthDate)}}</p>
                    </div>
                  </div>
                </div>

                <div *ngIf="!pets || pets.length === 0" class="empty-state">
                  <div class="empty-state-icon">&#x1F43E;</div>
                  <p class="empty-state-text">No pets registered yet</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class OwnerDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ownerApi = inject(OwnerApiService);
  private petApi = inject(PetApiService);

  owner: Owner | null = null;
  pets: Pet[] = [];
  ownerId: string = '';

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.ownerId = params['ownerId'];
      this.loadOwnerData();
      this.loadOwnerPets();
    });
  }

  loadOwnerData(): void {
    this.ownerApi.getOwnerById(this.ownerId).subscribe({
      next: (owner) => {
        this.owner = owner;
      },
      error: (_error) => {
      }
    });
  }

  loadOwnerPets(): void {
    this.ownerApi.getOwnerPets(this.ownerId).subscribe({
      next: (response) => {
        // Parse the Server-Sent Events format response
        const petResponses = response.split('\n');
        const petObjects = petResponses
          .map(petResponse => {
            const trimmedResponse = petResponse.replace(/^data:/, '').trim();
            if (!trimmedResponse) return null;
            try {
              return JSON.parse(trimmedResponse);
            } catch (error) {
              return null;
            }
          })
          .filter(pet => pet !== null);

        // Fetch detailed data for each pet
        if (petObjects.length > 0) {
          const petPromises = petObjects.map(pet => 
            this.petApi.getPetById(pet.petId)
          );

          forkJoin(petPromises).subscribe({
            next: (pets) => {
              this.pets = pets;
            },
            error: (_error) => {
            }
          });
        }
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

  deleteOwner(): void {
    const confirmed = confirm(`Are you sure you want to delete owner ${this.owner?.firstName} ${this.owner?.lastName}?`);
    if (confirmed && this.ownerId) {
      this.ownerApi.deleteOwner(this.ownerId).subscribe({
        next: () => {
          alert('Owner deleted successfully');
          this.router.navigate(['/owners']); // Navigate back to owner list
        },
        error: (_error) => {
          alert('Error deleting owner');
        }
      });
    }
  }

  toggleActiveStatus(petId: string): void {
    // Get fresh pet data like Angular JS
    this.petApi.getPetByIdFresh(petId).subscribe({
      next: (pet) => {
        // Toggle the active status
        pet.isActive = !pet.isActive;

        // Update the pet
        this.petApi.updatePet(petId, {
          name: pet.name,
          birthDate: pet.birthDate,
          petTypeId: pet.petTypeId,
          weight: pet.weight
        }).subscribe({
          next: (updatedPet) => {
            // Update the pet in the local array
            const index = this.pets.findIndex(p => p.petId === petId);
            if (index !== -1) {
              this.pets[index] = updatedPet;
            }
          },
          error: (_error) => {
          }
        });
      },
      error: (_error) => {
      }
    });
  }

  deletePet(petId: string): void {
    const confirmed = confirm('Are you sure you want to delete this pet?');
    if (confirmed) {
      this.petApi.deletePet(petId).subscribe({
        next: () => {
          // Remove pet from local array
          this.pets = this.pets.filter(pet => pet.petId !== petId);
          // Also remove from owner's pets if it exists
          if (this.owner?.pets) {
            this.owner.pets = this.owner.pets.filter(pet => pet.petId !== petId);
          }
        },
        error: (_error) => {
        }
      });
    }
  }
}

