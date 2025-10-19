import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PetApiService } from '../../api/pet-api.service';
import { PetType } from '../../models/pet.model';

@Component({
  selector: 'app-pet-type-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/pet.css" rel="stylesheet" type="text/css"/>

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">Pet Types</h1>
          <p class="page-subtitle">Manage and configure pet types in the system</p>
        </div>

        <div class="content-wrapper">
          <div class="add-pet-type-card">
            <button class="btn-add" (click)="toggleAddForm()">
              &#x2795; Add New Pet Type
            </button>

            <div *ngIf="showAddForm && newPetType" class="add-form">
              <form name="addForm" #addForm="ngForm" novalidate>
                <h5>Add New Pet Type</h5>
                <div class="form-group">
                  <label>Name:</label>
                  <input type="text"
                         class="form-control"
                         name="name"
                         [(ngModel)]="newPetType.name"
                         pattern="^[a-zA-Z\\s]+$"
                         required>
                  <small class="error-message" *ngIf="addForm.name?.$error?.pattern">
                    Letters and spaces only
                  </small>
                </div>
                <div class="form-group">
                  <label>Description:</label>
                  <input type="text"
                         class="form-control"
                         name="description"
                         [(ngModel)]="newPetType.petTypeDescription"
                         pattern="^[a-zA-Z\\s]+$"
                         required>
                  <small class="error-message" *ngIf="addForm.description?.$error?.pattern">
                    Letters and spaces only
                  </small>
                </div>
                <div class="form-actions">
                  <button type="button" class="btn-submit"
                          (click)="addPetType()">Save</button>
                  <button type="button" class="btn-cancel"
                          (click)="toggleAddForm()">Cancel</button>
                </div>
              </form>
            </div>
          </div>

          <!-- Filter & pagination controls -->
          <div class="filters-card">
            <div class="filters-header">
              <div class="filters-title">Search & Filter</div>
              <div class="action-buttons">
                <button class="btn-modern btn-search"
                        (click)="searchPetTypesByPaginationAndFilters()">&#x1F50D; Search</button>
                <button class="btn-modern btn-reset"
                        (click)="clearInputAndResetDefaultData()">&#x21BB; Reset</button>
              </div>
            </div>

            <div class="filters-grid">
              <div class="filter-group">
                <input id="petTypeIdInput" type="text" class="filter-input" placeholder="Enter Pet Type ID...">
                <label class="filter-label">Pet Type ID</label>
              </div>
              <div class="filter-group">
                <input id="nameInput" type="text" class="filter-input" placeholder="Enter name...">
                <label class="filter-label">Name</label>
              </div>
              <div class="filter-group d-none d-sm-flex">
                <input id="descriptionInput" type="text" class="filter-input" placeholder="Enter description...">
                <label class="filter-label">Description</label>
              </div>
              <div class="filter-group">
                <select id="sizeInput" class="filter-input" style="width: 100%">
                  <option value="">Select page size</option>
                  <option value="2">2 per page</option>
                  <option value="5">5 per page</option>
                  <option value="10">10 per page</option>
                  <option value="15">15 per page</option>
                </select>
                <label class="filter-label">Page Size</label>
              </div>
            </div>
          </div>

          <!-- Table showing pet types with inline update/delete -->
          <div class="table-card">
            <table id="petType" class="table table-modern">
              <thead>
                <tr>
                  <th>Pet Type ID</th>
                  <th>Pet Type Name</th>
                  <th class="d-none d-sm-table-cell d-md-table-cell">Description</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let petType of petTypes">
                  <td>
                    <a class="link-modern" [routerLink]="['/pet-types', petType.petTypeId]">
                      {{petType.petTypeId}}
                    </a>
                  </td>
                  <td class="responsive-text">
                    <span *ngIf="editingPetType !== petType.petTypeId">{{petType.name}}</span>
                    <input *ngIf="editingPetType === petType.petTypeId && editForm"
                           type="text"
                           [(ngModel)]="editForm.name"
                           class="form-control form-control-sm">
                  </td>
                  <td class="d-none d-sm-table-cell d-md-table-cell responsive-text">
                    <span *ngIf="editingPetType !== petType.petTypeId">{{petType.petTypeDescription}}</span>
                    <input *ngIf="editingPetType === petType.petTypeId && editForm"
                           type="text"
                           [(ngModel)]="editForm.petTypeDescription"
                           class="form-control form-control-sm">
                  </td>
                  <td>
                    <button *ngIf="editingPetType !== petType.petTypeId"
                            class="btn-sm btn-primary-sm"
                            (click)="editPetType(petType)">Update</button>
                    <button *ngIf="editingPetType === petType.petTypeId"
                            class="btn-sm btn-success-sm"
                            (click)="savePetType(petType.petTypeId)">Save</button>
                    <button *ngIf="editingPetType === petType.petTypeId"
                            class="btn-sm btn-secondary-sm"
                            (click)="cancelEdit()">Cancel</button>
                    <button class="btn-sm btn-danger-sm"
                            (click)="deletePetType(petType.petTypeId)">Delete</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="pagination-card">
            <nav aria-label="Page navigation">
              <ul class="pagination-modern">
                <li class="page-item-modern">
                  <a class="page-link-modern" aria-label="Previous" (click)="goPreviousPage()">
                    &#x2039;
                  </a>
                </li>
                <li class="page-item-modern">
                  <a class="page-number-link-modern" disabled>Page {{currentPageOnSite}}</a>
                </li>
                <li class="page-item-modern">
                  <a class="page-link-modern" aria-label="Next" (click)="goNextPage()">
                    &#x203A;
                  </a>
                </li>
              </ul>
            </nav>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class PetTypeListComponent implements OnInit {
  private petApi = inject(PetApiService);

  petTypes: PetType[] = [];
  allPetTypes: PetType[] = [];
  showAddForm = false;
  newPetType = { name: '', petTypeDescription: '' };

  editingPetType: string | null = null;
  editForm = { name: '', petTypeDescription: '' };

  currentPage = 0;
  pageSize = 5;
  currentPageOnSite = 1;
  totalItems = 0;
  totalPages = 0;

  // Search variables
  petTypeId: string | null = null;
  name: string | null = null;
  description: string | null = null;
  selectedSize: string | null = null;
  searchActive = false;

  ngOnInit(): void {
    this.loadDefaultData();
  }

  loadDefaultData(): void {
    this.petApi.getAllPetTypes().subscribe({
      next: (petTypes) => {
        this.allPetTypes = petTypes;
        this.petTypes = petTypes;
        this.totalItems = this.allPetTypes.length;
        this.totalPages = Math.ceil(this.totalItems / this.pageSize);
        this.applyPagination();
        this.updateCurrentPageOnSite();
      },
      error: (_error) => {
      }
    });
  }

  applyPagination(): void {
    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.petTypes = this.allPetTypes.slice(startIndex, endIndex);
  }

  toggleAddForm(): void {
    this.showAddForm = !this.showAddForm;
    if (!this.showAddForm) {
      this.newPetType = { name: '', petTypeDescription: '' };
    }
  }

  addPetType(): void {
    const lettersOnly = /^[a-zA-Z\s]+$/;

    if (!lettersOnly.test(this.newPetType.name) ||
        !lettersOnly.test(this.newPetType.petTypeDescription)) {
      alert('Name and Description must contain letters and spaces only.');
      return;
    }

    if (!this.newPetType.name || !this.newPetType.petTypeDescription) {
      alert('Name and Description are required.');
      return;
    }

    this.petApi.createPetType(this.newPetType).subscribe({
      next: () => {
        alert('Pet type added successfully!');
        this.showAddForm = false;
        this.newPetType = { name: '', petTypeDescription: '' };
        this.loadDefaultData();
      },
      error: (_error) => {
        alert('Failed to add pet type: ' + (_error.error?.message || 'Unknown error'));
      }
    });
  }

  editPetType(petType: PetType): void {
    this.editingPetType = petType.petTypeId;
    this.editForm = {
      name: petType.name,
      petTypeDescription: petType.petTypeDescription || ''
    };
  }

  savePetType(petTypeId: string): void {
    this.petApi.updatePetType(petTypeId, this.editForm).subscribe({
      next: () => {
        alert('Pet type updated successfully!');
        this.loadDefaultData();
        this.cancelEdit();
      },
      error: (_error) => {
        alert('Failed to update pet type: ' + (_error.error?.message || 'Unknown error'));
      }
    });
  }

  cancelEdit(): void {
    this.editingPetType = null;
    this.editForm = { name: '', petTypeDescription: '' };
  }

  deletePetType(petTypeId: string): void {
    const isConfirmed = confirm('Are you sure you want to delete this pet type?');
    if (isConfirmed) {
      this.petApi.deletePetType(petTypeId).subscribe({
        next: () => {
          alert('Pet type deleted successfully!');
          this.loadDefaultData();
        },
        error: (_error) => {
          alert('Failed to delete pet type: ' + (_error.error?.message || 'Unknown error'));
        }
      });
    }
  }

  searchPetTypesByPaginationAndFilters(_currentPage = 0, prevOrNextPressed = false): void {
    const sizeInput = document.getElementById('sizeInput') as HTMLSelectElement;
    this.selectedSize = sizeInput?.value || '';

    if (!prevOrNextPressed) {
      const petTypeIdInput = document.getElementById('petTypeIdInput') as HTMLInputElement;
      const nameInput = document.getElementById('nameInput') as HTMLInputElement;
      const descriptionInput = document.getElementById('descriptionInput') as HTMLInputElement;

      this.petTypeId = petTypeIdInput?.value || '';
      this.name = nameInput?.value || '';
      this.description = descriptionInput?.value || '';

      if (!this.petTypeId && !this.name && !this.description && !this.selectedSize) {
        alert('Oops! It seems like you forgot to enter any filter criteria.');
        return;
      }
    }

    this.searchActive = true;

    let filteredPetTypes = this.allPetTypes;

    if (this.petTypeId) {
      filteredPetTypes = filteredPetTypes.filter(petType => 
        petType.petTypeId && petType.petTypeId.toString().includes(this.petTypeId!)
      );
    }

    if (this.name) {
      filteredPetTypes = filteredPetTypes.filter(petType => 
        petType.name && petType.name.toLowerCase().includes(this.name!.toLowerCase())
      );
    }

    if (this.description) {
      filteredPetTypes = filteredPetTypes.filter(petType => 
        petType.petTypeDescription &&
        petType.petTypeDescription.toLowerCase().includes(this.description!.toLowerCase())
      );
    }

    if (this.selectedSize) {
      this.pageSize = Number(this.selectedSize);
    }

    if (!prevOrNextPressed) {
      this.currentPage = 0;
    }

    this.totalItems = filteredPetTypes.length;
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);

    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.petTypes = filteredPetTypes.slice(startIndex, endIndex);

    this.updateCurrentPageOnSite();
  }

  clearInputAndResetDefaultData(): void {
    const petTypeIdInput = document.getElementById('petTypeIdInput') as HTMLInputElement;
    const nameInput = document.getElementById('nameInput') as HTMLInputElement;
    const descriptionInput = document.getElementById('descriptionInput') as HTMLInputElement;
    const sizeInput = document.getElementById('sizeInput') as HTMLSelectElement;

    if (petTypeIdInput) petTypeIdInput.value = '';
    if (nameInput) nameInput.value = '';
    if (descriptionInput) descriptionInput.value = '';
    if (sizeInput) sizeInput.selectedIndex = 0;

    this.currentPage = 0;
    this.pageSize = 5;
    this.petTypeId = null;
    this.name = null;
    this.description = null;
    this.selectedSize = null;
    this.searchActive = false;

    this.petTypes = this.allPetTypes;
    this.totalItems = this.allPetTypes.length;
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    this.applyPagination();
    this.updateCurrentPageOnSite();

    alert('All filters have been cleared successfully.');
  }

  goNextPage(): void {
    if (this.currentPage + 1 < this.totalPages) {
      const currentPageInt = this.currentPage + 1;
      this.currentPage = currentPageInt;
      this.updateCurrentPageOnSite();

      if (this.searchActive) {
        this.searchPetTypesByPaginationAndFilters(currentPageInt, true);
      } else {
        this.applyPagination();
      }
    }
  }

  goPreviousPage(): void {
    if (this.currentPage > 0) {
      const currentPageInt = this.currentPage - 1;
      this.currentPage = currentPageInt;
      this.updateCurrentPageOnSite();

      if (this.searchActive) {
        this.searchPetTypesByPaginationAndFilters(currentPageInt, true);
      } else {
        this.applyPagination();
      }
    }
  }

  private updateCurrentPageOnSite(): void {
    this.currentPageOnSite = this.currentPage + 1;
  }
}