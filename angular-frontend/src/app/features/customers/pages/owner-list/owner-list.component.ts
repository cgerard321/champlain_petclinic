import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OwnerApiService } from '../../api/owner-api.service';
import { Owner } from '../../models/owner.model';

@Component({
  selector: 'app-owner-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <link href="/css/customers/owner.css" rel="stylesheet" type="text/css" />

    <div class="dashboard-container">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">Current Owners</h1>
          <p class="page-subtitle">Manage and view all registered owners</p>
        </div>

        <div class="content-wrapper">
          <!-- Filters Card -->
          <div class="filters-card">
            <div class="filters-header">
              <div class="filters-title">Search & Filter</div>
              <div class="action-buttons">
                <button
                  class="btn-modern btn-search"
                  (click)="searchOwnersByPaginationAndFilters()"
                >
                  &#x1F50D; Search
                </button>
                <button class="btn-modern btn-reset" (click)="clearInputAndResetDefaultData()">
                  &#x21BB; Reset
                </button>
              </div>
            </div>

            <div class="filters-grid">
              <div class="filter-group">
                <input
                  id="ownerIdInput"
                  type="text"
                  class="filter-input"
                  placeholder="Enter Owner ID..."
                />
                <label class="filter-label">Owner ID</label>
              </div>

              <div class="filter-group">
                <input
                  id="firstNameInput"
                  type="text"
                  class="filter-input"
                  placeholder="Enter first name..."
                />
                <label class="filter-label">First Name</label>
              </div>

              <div class="filter-group">
                <input
                  id="lastNameInput"
                  type="text"
                  class="filter-input"
                  placeholder="Enter last name..."
                />
                <label class="filter-label">Last Name</label>
              </div>

              <div class="filter-group d-none d-sm-flex">
                <input
                  id="cityInput"
                  type="text"
                  class="filter-input"
                  placeholder="Enter city..."
                />
                <label class="filter-label">City</label>
              </div>

              <div class="filter-group d-none d-sm-flex">
                <input
                  id="phoneNumberInput"
                  type="text"
                  class="filter-input"
                  placeholder="Enter phone..."
                />
                <label class="filter-label">Phone Number</label>
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

          <!-- Owners Table Card -->
          <div class="table-card">
            <table id="owner" class="table table-modern">
              <thead>
                <tr>
                  <th>Owner ID</th>
                  <th>Full Name</th>
                  <th class="d-none d-sm-table-cell d-md-table-cell">Address</th>
                  <th class="d-none d-sm-table-cell d-md-table-cell">City</th>
                  <th class="d-none d-sm-table-cell d-md-table-cell">Province</th>
                  <th class="d-none d-sm-table-cell d-md-table-cell">Phone Number</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let owner of owners">
                  <td>
                    <a class="link-modern" [routerLink]="['/owners', owner.ownerId]">
                      {{ owner.ownerId }}
                    </a>
                  </td>
                  <td class="responsive-text">{{ owner.firstName }} {{ owner.lastName }}</td>
                  <td class="d-none d-sm-table-cell d-md-table-cell responsive-text">
                    {{ owner.address }}
                  </td>
                  <td class="d-none d-sm-table-cell d-md-table-cell responsive-text">
                    {{ owner.city }}
                  </td>
                  <td class="d-none d-sm-table-cell d-md-table-cell responsive-text">
                    {{ owner.province }}
                  </td>
                  <td class="d-none d-sm-table-cell d-md-table-cell responsive-text">
                    {{ owner.telephone }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination Card -->
          <div class="pagination-card">
            <nav aria-label="Page navigation">
              <ul class="pagination-modern">
                <li class="page-item-modern">
                  <a class="page-link-modern" aria-label="Previous" (click)="goPreviousPage()">
                    &#x2039;
                  </a>
                </li>
                <li class="page-item-modern">
                  <a class="page-number-link-modern" disabled>Page {{ currentPageOnSite }}</a>
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
  styles: [],
})
export class OwnerListComponent implements OnInit {
  private ownerApi = inject(OwnerApiService);

  owners: Owner[] = [];
  currentPage = 0;
  pageSize: number | string = 5;
  totalItems = 0;
  totalPages = 0;
  currentPageOnSite = 1;
  searchActive = false;

  // Variables to match Angular JS controller
  ownerId: string | null = null;
  firstName: string | null = null;
  lastName: string | null = null;
  phoneNumber: string | null = null;
  city: string | null = null;
  selectedSize: string | null = null;

  ngOnInit(): void {
    this.loadDefaultData();
  }

  loadDefaultData(): void {
    if (!this.searchActive) {
      this.ownerApi.getOwnersCount().subscribe({
        next: count => {
          this.totalItems = count;
          const size = this.pageSize || 5;
          this.totalPages = Math.ceil(this.totalItems / Number(size));

          this.ownerApi.getOwnersPaginated().subscribe({
            next: owners => {
              this.owners = owners;
              this.updateCurrentPageOnSite();
            },
            error: () => {},
          });
        },
        error: () => {},
      });
    }
  }

  searchOwnersByPaginationAndFilters(currentPage = 0, prevOrNextPressed = false): void {
    // Collect search parameters from DOM like Angular JS
    const sizeInput = document.getElementById('sizeInput') as HTMLSelectElement;
    this.selectedSize = sizeInput?.value || '';

    if (!prevOrNextPressed) {
      const ownerIdInput = document.getElementById('ownerIdInput') as HTMLInputElement;
      const firstNameInput = document.getElementById('firstNameInput') as HTMLInputElement;
      const lastNameInput = document.getElementById('lastNameInput') as HTMLInputElement;
      const phoneNumberInput = document.getElementById('phoneNumberInput') as HTMLInputElement;
      const cityInput = document.getElementById('cityInput') as HTMLInputElement;

      this.ownerId = ownerIdInput?.value || '';
      this.firstName = firstNameInput?.value || '';
      this.lastName = lastNameInput?.value || '';
      this.phoneNumber = phoneNumberInput?.value || '';
      this.city = cityInput?.value || '';

      // Check if all input fields are empty
      if (this.checkIfAllInputFieldsAreEmptyOrNull()) {
        alert(
          'Oops! It seems like you forgot to enter any filter criteria. Please provide some filter input to continue.'
        );
        return;
      }
    }

    this.searchActive = true;

    // Build search filters
    const searchFilters: Record<string, unknown> = {
      page: currentPage,
      size: this.selectedSize || this.pageSize,
    };

    if (this.ownerId) searchFilters.ownerId = this.ownerId;
    if (this.firstName) searchFilters.firstName = this.firstName;
    if (this.lastName) searchFilters.lastName = this.lastName;
    if (this.phoneNumber) searchFilters.phoneNumber = this.phoneNumber;
    if (this.city) searchFilters.city = this.city;

    // Get filtered count
    const countFilters = { ...searchFilters };
    delete countFilters.page;
    delete countFilters.size;

    this.ownerApi.getFilteredOwnersCount(countFilters).subscribe({
      next: count => {
        this.totalItems = count;
        this.totalPages = Math.ceil(this.totalItems / Number(searchFilters.size));

        this.ownerApi.searchOwners(searchFilters).subscribe({
          next: owners => {
            this.owners = owners;
            this.updateCurrentPageOnSite();
          },
          error: () => {},
        });
      },
      error: () => {},
    });
  }

  clearInputAndResetDefaultData(): void {
    // Clear DOM inputs like Angular JS
    const ownerIdInput = document.getElementById('ownerIdInput') as HTMLInputElement;
    const firstNameInput = document.getElementById('firstNameInput') as HTMLInputElement;
    const lastNameInput = document.getElementById('lastNameInput') as HTMLInputElement;
    const phoneNumberInput = document.getElementById('phoneNumberInput') as HTMLInputElement;
    const cityInput = document.getElementById('cityInput') as HTMLInputElement;
    const sizeInput = document.getElementById('sizeInput') as HTMLSelectElement;

    if (firstNameInput) firstNameInput.value = '';
    if (lastNameInput) lastNameInput.value = '';
    if (ownerIdInput) ownerIdInput.value = '';
    if (phoneNumberInput) phoneNumberInput.value = '';
    if (cityInput) cityInput.value = '';
    if (sizeInput) sizeInput.selectedIndex = 0;

    this.currentPage = 0;
    this.pageSize = 5;

    this.ownerId = null;
    this.firstName = null;
    this.lastName = null;
    this.phoneNumber = null;
    this.city = null;
    this.selectedSize = null;

    this.searchActive = false;
    this.loadDefaultData();
    alert('All filters have been cleared successfully.');
  }

  goPreviousPage(): void {
    if (this.currentPage > 0) {
      const currentPageInt = this.currentPage - 1;
      this.currentPage = currentPageInt;
      this.updateCurrentPageOnSite();

      if (this.searchActive) {
        this.searchOwnersByPaginationAndFilters(currentPageInt, true);
      } else {
        this.loadDefaultData();
      }
    }
  }

  goNextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      const currentPageInt = this.currentPage + 1;
      this.currentPage = currentPageInt;
      this.updateCurrentPageOnSite();

      if (this.searchActive) {
        this.searchOwnersByPaginationAndFilters(currentPageInt, true);
      } else {
        this.loadDefaultData();
      }
    }
  }

  private updateCurrentPageOnSite(): void {
    this.currentPageOnSite = this.currentPage + 1;
  }

  private checkIfAllInputFieldsAreEmptyOrNull(): boolean {
    return (
      (this.ownerId === null || this.ownerId === '') &&
      (this.firstName === null || this.firstName === '') &&
      (this.lastName === null || this.lastName === '') &&
      (this.phoneNumber === null || this.phoneNumber === '') &&
      (this.city === null || this.city === '') &&
      (this.selectedSize === null || this.selectedSize === '')
    );
  }
}
