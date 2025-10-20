import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InventoryApiService } from '../../api/inventory-api.service';
import { InventoryRequest } from '../../models/inventory.model';

@Component({
  selector: 'app-inventories-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bColor text-center">
      <h2 class="titleBundle form" id="title">Add new Inventory</h2>
    </div>
    <div class="p-3 formColor m-0">
      <div id="inventoryForm" class="form-horizontal" enctype="multipart/form-data">
        <div class="row">
          <div class="col-sm-6 form-group">
            <label class="control-label" for="inventoryName">Inventory name</label>
            <input
              class="form-control"
              id="inventoryName"
              [(ngModel)]="inventory.inventoryName"
              name="inventoryName"
              type="text"
              required
              title="Please select a type of inventory."
            />
          </div>
        </div>
        <div class="col-sm-6 form-group">
          <label class="control-label" for="inventoryType">Inventory Type</label>
          <input
            class="form-control col-sm-4"
            type="text"
            placeholder="Search"
            [(ngModel)]="inventoryTypeFormSearch"
            (ngModelChange)="updateOption()"
          />
          <select
            class="form-control col-sm-4"
            id="inventoryType"
            [(ngModel)]="selectedOption"
            name="inventoryType"
            required
            title="Please select the inventory type."
          >
            <option *ngFor="let option of inventoryTypeOptions" [value]="option">
              {{ option }}
            </option>
          </select>
        </div>
        <div class="row">
          <div class="form-group col-sm-12">
            <label class="control-label text-center" for="invDesc">Inventory Description</label>
            <input
              class="form-control"
              id="invDesc"
              [(ngModel)]="inventory.inventoryDescription"
              name="inventoryDescription"
              type="text"
              required
              title="Please select a date."
            />
          </div>
        </div>
        <div class="form-group p-3">
          <div class="bundle marg col-sm-12">
            <button
              id="newBtn"
              class="w-100 btn btn-primary btn-lg"
              type="button"
              (click)="submitInventoryForm()"
            >
              Submit
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class InventoriesFormComponent implements OnInit {
  private inventoryApi = inject(InventoryApiService);
  private router = inject(Router);

  inventory: InventoryRequest = {
    inventoryName: '',
    inventoryType: '',
    inventoryDescription: '',
  };

  inventoryTypeFormSearch: string = '';
  inventoryTypeOptions: string[] = ['New Type'];
  selectedOption: string = 'New Type';

  ngOnInit(): void {
    this.loadInventoryTypes();
  }

  private loadInventoryTypes(): void {
    this.inventoryApi.getInventoryTypes().subscribe({
      next: types => {
        types.forEach((type: any) => {
          this.inventoryTypeOptions.push(type.type);
        });
        if (!this.selectedOption) {
          this.selectedOption = this.inventoryTypeOptions[0];
        }
      },
      error: _error => this.handleHttpError(_error),
    });
  }

  submitInventoryForm(): void {
    if (this.selectedOption === 'New Type' && this.inventoryTypeFormSearch === '') {
      alert('Search field cannot be empty when you want to add a new type');
      return;
    }

    let data: InventoryRequest;

    if (this.selectedOption === 'New Type') {
      this.selectedOption = this.inventoryTypeFormSearch;

      data = {
        inventoryName: this.inventory.inventoryName,
        inventoryType: this.selectedOption,
        inventoryDescription: this.inventory.inventoryDescription,
      };

      // First create the new type, then create the inventory
      this.inventoryApi.createInventoryType({ name: this.selectedOption }).subscribe({
        next: () => {
          this.inventoryApi.createInventory(data).subscribe({
            next: () => {
              this.router.navigate(['/inventories']);
            },
            error: _error => this.handleHttpError(_error),
          });
        },
        error: _error => this.handleHttpError(_error),
      });
    } else {
      data = {
        inventoryName: this.inventory.inventoryName,
        inventoryType: this.selectedOption,
        inventoryDescription: this.inventory.inventoryDescription,
      };

      this.inventoryApi.createInventory(data).subscribe({
        next: () => {
          this.router.navigate(['/inventories']);
        },
        error: _error => this.handleHttpError(_error),
      });
    }
  }

  updateOption(): void {
    const searchLowerCase = this.inventoryTypeFormSearch.toLowerCase();
    this.selectedOption = this.inventoryTypeOptions[0];

    for (let i = 0; i < this.inventoryTypeOptions.length; i++) {
      const optionLowerCase = this.inventoryTypeOptions[i].toLowerCase();
      if (optionLowerCase.indexOf(searchLowerCase) !== -1) {
        this.selectedOption = this.inventoryTypeOptions[i];
        break;
      }
    }
  }

  private handleHttpError(response: { data?: unknown; status?: number }): void {
    let data = response && response.data;
    const status = response && response.status;
    const statusText = (response && (response as any).statusText) || '';

    // Normalize string bodies (plain text or JSON-as-string)
    if (typeof data === 'string') {
      try {
        data = JSON.parse(data);
      } catch (e) {
        const plain = (data as string).trim();
        if (plain) {
          alert(plain);
          return;
        }
        data = {};
      }
    }
    data = data || {};

    // Arrays the backend might use
    const errorsArr = Array.isArray((data as any).errors) ? (data as any).errors : [];
    const detailsArr = Array.isArray((data as any).details) ? (data as any).details : [];
    const violations = Array.isArray((data as any).violations || (data as any).constraintViolations)
      ? (data as any).violations || (data as any).constraintViolations
      : [];

    function mapErr(e: {
      field?: string;
      path?: string;
      parameter?: string;
      property?: string;
      defaultMessage?: string;
      message?: string;
      reason?: string;
      detail?: string;
      title?: string;
    }): string {
      if (typeof e === 'string') return e;
      const field = e.field || e.path || e.parameter || e.property || '';
      const msg = e.defaultMessage || e.message || e.reason || e.detail || e.title || '';
      const asStr = msg || JSON.stringify(e);
      return field ? field + ': ' + asStr : asStr;
    }

    const fieldText = ([] as string[])
      .concat(errorsArr.map(mapErr))
      .concat(detailsArr.map(mapErr))
      .concat(violations.map(mapErr))
      .filter(Boolean)
      .join('\r\n');

    const baseMsg =
      (data as any).message ||
      (data as any).error_description ||
      (data as any).errorMessage ||
      (data as any).error ||
      (data as any).title ||
      (data as any).detail ||
      (typeof data === 'object' && data && Object.keys(data).length ? JSON.stringify(data) : '') ||
      (status ? 'HTTP ' + status + (statusText ? ' ' + statusText : '') : 'Request failed');

    alert(fieldText ? baseMsg + '\r\n' + fieldText : baseMsg);
  }
}
