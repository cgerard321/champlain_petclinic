import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryApiService } from '../../api/inventory-api.service';
import { Inventory, InventoryRequest } from '../../models/inventory.model';

@Component({
  selector: 'app-inventories-update-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bColor text-center"><h2 class="titleBundle form" id="title">Update Inventory</h2></div>
    <div class="p-3 formColor m-0" *ngIf="inventory">
      <div id="inventoryUpdateForm" class="form-horizontal" enctype="multipart/form-data">
        <div class="row">
          <div class="col-sm-6 form-group">
            <label class="control-label" for="inventoryName">Inventory name</label>
            <input class="form-control" id="inventoryName" [(ngModel)]="inventory.inventoryName" name="inventoryName" type="text" required title="Please select a type of inventory."/>
          </div>
        </div>
        <div class="col-sm-6 form-group">
          <label class="control-label" for="inventoryType">Inventory Type</label>
          <input class="form-control col-sm-4" type="text" placeholder="Search" [(ngModel)]="inventoryTypeFormUpdateSearch" (ngModelChange)="updateOptionUpdate()">
          <select class="form-control col-sm-4" id="inventoryType" [(ngModel)]="selectedUpdateOption" name="inventoryType" required title="Please select the inventory type.">
            <option *ngFor="let option of inventoryTypeUpdateOptions" [value]="option">{{option}}</option>
          </select>
        </div>
        <div class="row">
          <div class="form-group col-sm-12">
            <label class="control-label text-center" for="invDesc">Inventory Description</label>
            <input class="form-control" id="invDesc" [(ngModel)]="inventory.inventoryDescription" name="inventoryDescription" type="text" required title="Please select a date."/>
          </div>
        </div>
        <div class="form-group p-3">
          <div class="bundle marg col-sm-12">
            <button id="newBtn" class="w-100 btn btn-primary btn-lg" type="button" (click)="submitUpdateInventoryForm()">Submit</button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class InventoriesUpdateFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private inventoryApi = inject(InventoryApiService);

  inventoryId: string = '';
  inventory: Inventory | null = null;
  method: string = 'edit';
  
  inventoryTypeFormUpdateSearch: string = '';
  inventoryTypeUpdateOptions: string[] = ['New Type'];
  selectedUpdateOption: string = 'New Type';

  ngOnInit(): void {
    this.inventoryId = this.route.snapshot.paramMap.get('id') || '';
    this.loadInventory();
  }

  private loadInventory(): void {
    this.inventoryApi.getInventoryById(this.inventoryId).subscribe({
      next: (inventory) => {
        this.inventory = inventory;
        this.loadInventoryTypes();
      },
      error: (_error) => this.handleHttpError(_error)
    });
  }

  private loadInventoryTypes(): void {
    this.inventoryApi.getInventoryTypes().subscribe({
      next: (types) => {
        types.forEach((type: any) => {
          this.inventoryTypeUpdateOptions.push(type.type);
        });
        
        const inventoryType = this.inventory?.inventoryType;
        if (inventoryType && this.inventoryTypeUpdateOptions.includes(inventoryType)) {
          this.selectedUpdateOption = inventoryType;
        } else {
          this.inventoryTypeFormUpdateSearch = this.inventoryTypeUpdateOptions[0];
        }
      },
      error: (_error) => this.handleHttpError(_error)
    });
  }

  submitUpdateInventoryForm(): void {
    if (!this.inventory) return;

    if (this.selectedUpdateOption === "New Type" && this.inventoryTypeFormUpdateSearch === "") {
      alert("Search field cannot be empty when you want to add a new type");
      return;
    }

    let data: InventoryRequest;

    if (this.selectedUpdateOption === "New Type") {
      this.selectedUpdateOption = this.inventoryTypeFormUpdateSearch;
      data = {
        inventoryName: this.inventory.inventoryName,
        inventoryType: this.selectedUpdateOption,
        inventoryDescription: this.inventory.inventoryDescription
      };

      this.inventoryApi.createInventoryType({ name: this.selectedUpdateOption }).subscribe({
        next: () => {
          if (this.method === 'edit') {
            this.inventoryApi.updateInventory(this.inventoryId, data).subscribe({
              next: () => {
                this.router.navigate(['/inventories']);
              },
              error: (_error) => this.handleHttpError(_error)
            });
          } else {
          }
        },
        error: (_error) => this.handleHttpError(_error)
      });
    } else {
      data = {
        inventoryName: this.inventory.inventoryName,
        inventoryType: this.selectedUpdateOption,
        inventoryDescription: this.inventory.inventoryDescription
      };

      if (this.method === 'edit') {
        this.inventoryApi.updateInventory(this.inventoryId, data).subscribe({
          next: () => {
            this.router.navigate(['/inventories']);
          },
          error: (_error) => this.handleHttpError(_error)
        });
      } else {
      }
    }
  }

  updateOptionUpdate(): void {
    const searchLowerCase = this.inventoryTypeFormUpdateSearch.toLowerCase();
    this.selectedUpdateOption = this.inventoryTypeUpdateOptions[0];
    
    for (let i = 0; i < this.inventoryTypeUpdateOptions.length; i++) {
      const optionLowerCase = this.inventoryTypeUpdateOptions[i].toLowerCase();
      if (optionLowerCase.indexOf(searchLowerCase) !== -1) {
        this.selectedUpdateOption = this.inventoryTypeUpdateOptions[i];
        break;
      }
    }
  }

  private handleHttpError(response: any): void {
    try { 
    } catch (e) {}

    let data = response && response.data;
    const status = response && response.status;
    const statusText = (response && response.statusText) || '';

    if (typeof data === 'string') {
      try {
        data = JSON.parse(data);
      } catch (e) {
        const plain = data.trim();
        if (plain) {
          alert(plain);
          return;
        }
        data = {};
      }
    }
    data = data || {};

    const violations = data.violations || data.constraintViolations || [];
    const detailsArr = Array.isArray(data.details) ? data.details : [];
    const errorsArr = Array.isArray(data.errors) ? data.errors : [];

    function mapErr(e: any) {
      if (typeof e === 'string') return e;
      const field = e.field || e.path || e.parameter || e.property || '';
      const msg = e.defaultMessage || e.message || e.reason || e.detail || e.title || '';
      const asStr = msg || JSON.stringify(e);
      return field ? (field + ': ' + asStr) : asStr;
    }

    const fieldText = ([] as string[])
      .concat(errorsArr.map(mapErr))
      .concat(detailsArr.map(mapErr))
      .concat(Array.isArray(violations) ? violations.map(mapErr) : [])
      .filter(Boolean)
      .join('\r\n');

    const baseMsg =
      data.message ||
      data.error_description ||
      data.errorMessage ||
      data.error ||
      data.title ||
      data.detail ||
      (typeof data === 'object' && Object.keys(data).length === 0 ? '' : JSON.stringify(data)) ||
      (status ? ('HTTP ' + status + (statusText ? (' ' + statusText) : '')) : 'Request failed');

    alert(fieldText ? (baseMsg + '\r\n' + fieldText) : baseMsg);
  }
}


