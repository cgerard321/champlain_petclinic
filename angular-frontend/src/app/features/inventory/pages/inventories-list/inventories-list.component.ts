import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { InventoryApiService } from '../../api/inventory-api.service';
import { Inventory } from '../../models/inventory.model';

@Component({
  selector: 'app-inventories-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <style>
      .table-striped tbody tr:hover {
        background-color: #d8d8d8;
      }
      .btn:hover {
        transform: translateY(2px);
        box-shadow: 0 0 rgba(0, 0, 0, 2);
        border-bottom-width: 1px;
      }
    </style>

    <h2>Inventory</h2>

    <table class="table table-striped">
      <thead>
        <tr>
          <td>Inventory ID</td>
          <td>Name</td>
          <td>Type</td>
          <td>Description</td>
          <td>Updates</td>
          <td></td>
          <td></td>
          <td></td>
        </tr>
        <tr>
          <td>
            <input
              type="text"
              [(ngModel)]="inventoryCode"
              placeholder="INV-XXXX"
              (keyup.enter)="searchInventory()"
            />
          </td>
          <td>
            <input type="text" [(ngModel)]="inventoryName" (keyup.enter)="searchInventory()" />
          </td>
          <td>
            <select
              class="form-control col-sm-4"
              id="inventoryType"
              [(ngModel)]="inventoryType"
              name="inventoryType"
              (keyup.enter)="searchInventory()"
            >
              <option value=""></option>
              <option *ngFor="let type of inventoryTypeOptions" [value]="type">{{ type }}</option>
            </select>
          </td>
          <td>
            <input
              type="text"
              [(ngModel)]="inventoryDescription"
              (keyup.enter)="searchInventory()"
            />
          </td>
          <td></td>
          <td>
            <a class="btn btn-success" type="button" (click)="clearQueries()" title="Clear">
              <lord-icon
                src="https://cdn.lordicon.com/zxvuvcnc.json"
                trigger="hover"
                style="width:32px;height:32px"
              ></lord-icon>
            </a>
          </td>
          <td>
            <a class="btn btn-success" type="button" (click)="searchInventory()" title="Search">
              <lord-icon
                src="https://cdn.lordicon.com/fkdzyfle.json"
                trigger="hover"
                style="width:32px;height:32px"
              ></lord-icon>
            </a>
          </td>
          <td></td>
        </tr>
      </thead>

      <tbody>
        <tr
          *ngFor="let inventory of inventoryList"
          (click)="navigateToProducts(inventory.inventoryId)"
        >
          <td>
            <span>{{ inventory.inventoryCode }}</span>
          </td>

          <td>
            <span>
              <a
                style="text-decoration: none;"
                [routerLink]="['/inventories', inventory.inventoryId, 'products']"
                (click)="$event.stopPropagation()"
              >
                {{ inventory.inventoryName }}
              </a>
            </span>
          </td>

          <td>
            <span>{{ inventory.inventoryType }}</span>
          </td>
          <td>
            <span>{{ inventory.inventoryDescription }}</span>
          </td>

          <!-- NEW: last update message (Show Supply Updates) -->
          <td>
            <span>{{ inventory.recentUpdateMessage || '—' }}</span>
          </td>

          <!-- Edit -->
          <td>
            <a
              class="btn btn-warning"
              [routerLink]="['/inventories', inventory.inventoryId, 'edit']"
              (click)="$event.stopPropagation()"
              title="Edit"
            >
              <lord-icon
                src="https://cdn.lordicon.com/wkvacbiw.json"
                trigger="hover"
                style="width:32px;height:32px"
              ></lord-icon>
            </a>
          </td>

          <!-- Delete / Restore -->
          <td>
            <a
              *ngIf="!inventory.isTemporarilyDeleted"
              class="btn btn-danger"
              href="javascript:void(0)"
              (click)="deleteInventory(inventory); $event.stopPropagation()"
              title="Delete"
            >
              <lord-icon
                src="https://cdn.lordicon.com/skkahier.json"
                trigger="hover"
                style="width:32px;height:32px"
              ></lord-icon>
            </a>

            <a
              *ngIf="inventory.isTemporarilyDeleted"
              class="btn btn-info"
              href="javascript:void(0)"
              (click)="undoDelete(inventory); $event.stopPropagation()"
              title="Restore"
            >
              Restore
            </a>
          </td>

          <!-- spare last cell (kept like your layout) -->
          <td></td>
        </tr>
      </tbody>
    </table>

    <div class="text-center">
      <table class="mx-auto">
        <tr>
          <td><a class="btn btn-success btn-sm" (click)="pageBefore()"><</a></td>
          <td>
            <span>{{ realPage }}</span>
          </td>
          <td><a class="btn btn-success btn-sm" (click)="pageAfter()">></a></td>
        </tr>
      </table>
    </div>

    <div id="loadingObject" style="display: none;">Loading...</div>
    <div
      id="notification"
      style="display: none; position: fixed; bottom: 10px; right: 10px; background-color: #4CAF50; color: white; padding: 10px; border-radius: 5px;"
    >
      Notification Text Here
    </div>

    <a (click)="deleteAllInventories()">
      <button class="delete-bundle-button btn btn-success">Delete All Inventory</button>
    </a>

    <a routerLink="/inventories/new">
      <button class="add-inventory-button btn btn-success">Add Inventory</button>
    </a>
  `,
  styles: [
    `
      .table-striped tbody tr:hover {
        background-color: #d8d8d8;
      }
      .btn:hover {
        transform: translateY(2px);
        box-shadow: 0 0 rgba(0, 0, 0, 2);
        border-bottom-width: 1px;
      }
    `,
  ],
})
export class InventoriesListComponent implements OnInit {
  private inventoryApi = inject(InventoryApiService);
  private router = inject(Router);

  inventoryList: Inventory[] = [];
  inventoryTypeOptions: string[] = [];

  // Search parameters
  inventoryCode: string = '';
  inventoryName: string = '';
  inventoryType: string = '';
  inventoryDescription: string = '';

  // Pagination
  currentPage: number = 0;
  listSize: number = 10;
  realPage: number = 1;
  numberOfPage: number = 0;

  // Search state
  private name: string = '';
  private code: string = '';
  private type: string = '';
  private desc: string = '';

  ngOnInit(): void {
    this.getInventoryList();
    this.loadInventoryTypes();
  }

  private getInventoryList(): void {
    this.inventoryApi.getAllInventories(this.currentPage, this.listSize).subscribe({
      next: inventories => {
        this.inventoryList = inventories;
        this.numberOfPage = Math.ceil(inventories.length / 10);
      },
      error: error => {
        if (error.status === 404) {
          alert('inventory not found.');
        } else {
          alert('An error occurred: ' + error.statusText);
        }
      },
    });
  }

  private loadInventoryTypes(): void {
    this.inventoryApi.getInventoryTypes().subscribe({
      next: types => {
        this.inventoryTypeOptions = types.map((type: { type: string }) => type.type);
      },
      error: () => {},
    });
  }

  searchInventory(): void {
    this.getInventoryListWithSearch();
  }

  private getInventoryListWithSearch(): void {
    let queryString = '';
    this.name = '';
    this.code = '';
    this.type = '';
    this.desc = '';

    if (this.inventoryCode != null && this.inventoryCode !== '') {
      this.code = this.inventoryCode.toUpperCase();
      queryString += 'inventoryCode=' + this.code;
    }

    if (this.inventoryName != null && this.inventoryName !== '') {
      this.name = this.inventoryName;
      queryString += 'inventoryName=' + this.inventoryName;
    }

    if (this.inventoryType) {
      if (queryString !== '') {
        queryString += '&';
      }
      this.type = this.inventoryType;
      queryString += 'inventoryType=' + this.inventoryType;
    }

    if (this.inventoryDescription) {
      if (queryString !== '') {
        queryString += '&';
      }
      this.desc = this.inventoryDescription;
      queryString += 'inventoryDescription=' + this.inventoryDescription;
    }

    const searchParams = {
      inventoryCode: this.code,
      inventoryName: this.name,
      inventoryType: this.type,
      inventoryDescription: this.desc,
    };

    if (queryString !== '') {
      this.currentPage = 0;
      this.realPage = this.currentPage + 1;

      this.inventoryApi.getAllInventories(this.currentPage, this.listSize, searchParams).subscribe({
        next: inventories => {
          this.numberOfPage = Math.ceil(inventories.length / 10);
          this.inventoryList = inventories;
        },
        error: error => {
          if (error.status === 404) {
            alert('inventory not found.');
          } else {
            alert('An error occurred: ' + error.statusText);
          }
        },
      });
    } else {
      this.inventoryApi.getAllInventories(this.currentPage, this.listSize).subscribe({
        next: inventories => {
          this.numberOfPage = Math.ceil(inventories.length / 10);
          this.inventoryList = inventories;
        },
        error: error => {
          if (error.status === 404) {
            alert('inventory not found.');
          } else {
            alert('An error occurred: ' + error.statusText);
          }
        },
      });
    }
  }

  clearQueries(): void {
    this.inventoryName = '';
    this.inventoryCode = '';
    this.inventoryType = '';
    this.inventoryDescription = '';
    this.searchInventory();
  }

  deleteAllInventories(): void {
    const varIsConf = confirm('Are you sure you want to clear all entries from the inventory?');
    if (varIsConf) {
      this.inventoryApi.deleteAllInventories().subscribe({
        next: () => {
          alert('All inventory entries have been cleared!');
          this.inventoryList = [];
        },
        error: error => {
          alert(error.data?.errors || 'Failed to clear inventory entries.');
        },
      });
    }
  }

  deleteInventory(inventory: Inventory): void {
    const ifConfirmed = confirm('Are you sure you want to remove this inventory?');
    if (ifConfirmed) {
      // Step 1: Mark as temporarily deleted on frontend.
      inventory.isTemporarilyDeleted = true;

      // Display an Undo button for say, 5 seconds.
      setTimeout(() => {
        if (inventory.isTemporarilyDeleted) {
          // If it's still marked as deleted after 5 seconds, proceed with actual deletion.
          this.proceedToDelete(inventory);
        }
      }, 5000); // 5 seconds = 5000ms.
    }
  }

  undoDelete(inventory: Inventory): void {
    inventory.isTemporarilyDeleted = false;
    // Hide the undo button.
  }

  private proceedToDelete(inventory: Inventory): void {
    if (!inventory.isTemporarilyDeleted) return; // In case the user clicked undo just before the timeout.

    this.inventoryApi.deleteInventory(inventory.inventoryId).subscribe({
      next: () => {
        this.showNotification(
          inventory.inventoryCode +
            ' - ' +
            inventory.inventoryName +
            ' has been deleted successfully!'
        );
        // Then, after displaying the notification for 5 seconds, reload the page
        setTimeout(() => {
          location.reload();
        }, 1000);
      },
      error: error => {
        alert(error.data?.errors || 'Data is inaccessible.');
      },
    });
  }

  private showNotification(message: string): void {
    const notificationElement = document.getElementById('notification');
    if (notificationElement) {
      notificationElement.innerHTML = message;
      notificationElement.style.display = 'block';

      setTimeout(() => {
        notificationElement.style.display = 'none';
      }, 5000); // Hide after 5 seconds
    }
  }

  pageBefore(): void {
    if (this.currentPage - 1 >= 0) {
      this.currentPage = this.currentPage - 1;
      this.realPage = this.currentPage + 1;
      this.getInventoryListWithSearch();
    }
  }

  pageAfter(): void {
    if (this.currentPage + 1 <= this.numberOfPage) {
      this.currentPage = this.currentPage + 1;
      this.realPage = this.currentPage + 1;
      this.getInventoryListWithSearch();
    }
  }

  navigateToProducts(inventoryId: string): void {
    this.router.navigate(['/inventories', inventoryId, 'products']);
  }
}
