import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PetApiService } from '../../api/pet-api.service';
import { OwnerApiService } from '../../api/owner-api.service';
import { Pet, PetRequest } from '../../models/pet.model';

@Component({
  selector: 'app-pet-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/pet.css" rel="stylesheet" type="text/css"/>

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">{{isEditMode ? 'Edit Pet' : 'Add Pet'}}</h1>
          <p class="page-subtitle">{{isEditMode ? 'Update pet information' : 'Register a new pet'}}</p>
        </div>

        <div class="content-wrapper">
          <ng-container *ngIf="pet">
            <div class="form-card">
              <form id="petForm" class="pet-form" #petForm="ngForm" (ngSubmit)="submit()">
              <input type="hidden" [(ngModel)]="pet.petId" name="petId" />

              <div class="form-grid">
                <div class="form-group">
                  <label class="form-label">Pet Name</label>
                  <input class="form-input" [(ngModel)]="pet.name" name="name" required type="text" [disabled]="checked"/>
                  <span *ngIf="petForm.name?.$error?.required" class="error-message">Name is required.</span>
                </div>

                <div class="form-group">
                  <label class="form-label">Birth Date</label>
                  <input class="form-input" [(ngModel)]="pet.birthDate" name="dbo" required type="date" [disabled]="checked"/>
                  <span *ngIf="petForm.name?.$error?.required" class="error-message">Birth date is required.</span>
                </div>

                <div class="form-group">
                  <label class="form-label">Pet Weight (Kilograms)</label>
                  <input class="form-input" [(ngModel)]="pet.weight" name="weight" required type="text" [disabled]="checked"/>
                  <span *ngIf="petForm.name?.$error?.required" class="error-message">Weight is required.</span>
                </div>

                <div class="form-group">
                  <label class="form-label">Pet Type</label>
                  <select class="form-select" [(ngModel)]="pet.petTypeId" name="type" [disabled]="checked">
                    <option value="1">Cat</option>
                    <option value="2">Dog</option>
                    <option value="3">Lizard</option>
                    <option value="4">Snake</option>
                    <option value="5">Bird</option>
                    <option value="6">Hamster</option>
                  </select>
                </div>
              </div>

              <div class="form-actions">
                <button class="btn-submit" type="submit" [disabled]="!isFormValid()">
                  {{isEditMode ? 'Update Pet' : 'Create Pet'}}
                </button>
              </div>

              <div class="form-help">
                <p class="form-help-text">Make sure all information is accurate before submitting</p>
              </div>
            </form>
            </div>
          </ng-container>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class PetFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private petApi = inject(PetApiService);
  private ownerApi = inject(OwnerApiService);

  pet: Pet = {
    petId: '',
    name: '',
    birthDate: '',
    petTypeId: '',
    ownerId: '',
    isActive: true,
    weight: 0
  };

  owner: string = '';

  ownerId: string = '';
  petId: string = '';
  checked: boolean = false;
  isEditMode: boolean = false;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.ownerId = params['ownerId'] || '';
      this.petId = params['petId'] || '';
      this.isEditMode = !!this.petId;
      
      if (this.petId) {
        this.loadPetData();
      }
      
      if (this.ownerId) {
        this.loadOwnerData();
      }
    });
  }

  loadPetData(): void {
    this.petApi.getPetById(this.petId).subscribe({
      next: (pet) => {
        this.pet = pet;
        this.pet.birthDate = new Date(pet.birthDate).toISOString().split('T')[0]; // Format for date input
        this.checked = false;
      },
      error: (_error) => {
      }
    });
  }

  loadOwnerData(): void {
    this.ownerApi.getOwnerById(this.ownerId).subscribe({
      next: (owner) => {
        this.pet.ownerId = owner.ownerId;
        this.owner = `${owner.firstName} ${owner.lastName}`;
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

  submit(): void {
    const petTypeName = this.getPetTypeName(this.pet.petTypeId);
    const birthDate = new Date(this.pet.birthDate);
    const offset = birthDate.getTimezoneOffset();
    birthDate.setMinutes(birthDate.getMinutes() - offset);
    const formattedBirthDate = birthDate.toISOString().split('T')[0];
    
    if (confirm("Are you sure you want to submit this form with the following details?\n\n" +
        "Pet Name: " + this.pet.name + "\n" +
        "Pet Birth Date: " + formattedBirthDate + "\n" +
        "Weight: " + this.pet.weight + " KG" + "\n" +
        "Pet Type: " + petTypeName)) {

      const petRequest: PetRequest = {
        name: this.pet.name,
        birthDate: new Date(this.pet.birthDate).toISOString(),
        petTypeId: this.pet.petTypeId,
        weight: this.pet.weight
      };

      this.petApi.updatePet(this.petId, petRequest).subscribe({
        next: () => {
          this.router.navigate(['/owners', this.pet.ownerId, 'pets', this.petId]);
        },
        error: (_error) => {
          this.handleError(_error);
        }
      });
    }
  }

  private handleError(error: any): void {
    const errorData = error.error || {};
    const errors = errorData.errors || [];
    const errorMessage = errorData.error || 'An error occurred';
    
    const errorMessages = errors
      .map((e: any) => `${e.field}: ${e.defaultMessage}`)
      .join('\n');
    
    alert(errorMessage + (errorMessages ? '\n' + errorMessages : ''));
  }

  isFormValid(): boolean {
    return !!(this.pet.name && this.pet.birthDate && this.pet.petTypeId && this.pet.weight);
  }
}