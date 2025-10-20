import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { InventoryApiService } from '../../api/inventory-api.service';
import { InventoryProduct, Inventory } from '../../models/inventory.model';

@Component({
  selector: 'app-inventories-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <style>
      .btn:hover {
        transform: translateY(2px);
        box-shadow: 0 0 rgba(0, 0, 0, 2);
        border-bottom-width: 1px;
      }

      .table-striped tbody tr:hover {
        background-color: #d8d8d8;
      }
    </style>

    <h2>Inventory Products</h2>
    <p *ngIf="inventory">{{ inventory.inventoryCode }} - {{ inventory.inventoryName }}</p>

    <table class="table table-striped">
      <thead>
        <tr>
          <td>Name</td>
          <td>Id</td>
          <td>Quantity</td>
          <td>Price</td>
          <td>Description</td>
          <td>Sale Price</td>
          <td></td>
          <td></td>
          <td></td>
        </tr>
        <tr>
          <td>
            <span>
              <input type="text" [(ngModel)]="productName" (keyup.enter)="searchProduct()" />
            </span>
          </td>
          <td></td>
          <td>
            <span>
              <input
                type="number"
                min="0"
                [(ngModel)]="productQuantity"
                (keyup.enter)="searchProduct()"
              />
            </span>
          </td>
          <td>
            <span>
              <input
                type="number"
                step="0.01"
                min="0"
                [(ngModel)]="productPrice"
                (keyup.enter)="searchProduct()"
              />
            </span>
          </td>
          <td></td>
          <td>
            <span>
              <input
                type="number"
                step="0.01"
                min="0"
                [(ngModel)]="productSalePrice"
                (keyup.enter)="searchProduct()"
              />
            </span>
          </td>
          <td></td>
          <td>
            <span>
              <a class="btn btn-success" type="button" (click)="clearQueries()" title="Clear">
                <lord-icon
                  src="https://cdn.lordicon.com/zxvuvcnc.json"
                  trigger="hover"
                  style="width:32px;height:32px"
                >
                </lord-icon>
              </a>
            </span>
          </td>
          <td>
            <span>
              <a class="btn btn-success" type="button" (click)="searchProduct()" title="Search">
                <lord-icon
                  src="https://cdn.lordicon.com/fkdzyfle.json"
                  trigger="hover"
                  style="width:32px;height:32px"
                >
                </lord-icon>
              </a>
            </span>
          </td>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let product of inventoryProductList">
          <td>
            <span>{{ product.productName }}</span>
          </td>
          <td>
            <span>{{ product.productId }}</span>
          </td>
          <td>
            <span>{{ product.productQuantity }}</span>
          </td>
          <td>
            <span>{{ product.productPrice }}$</span>
          </td>
          <td>
            <span>{{ product.productDescription }}</span>
          </td>
          <td>
            <span>{{ product.productSalePrice }}$</span>
          </td>

          <td>
            <a
              class="btn btn-info"
              [routerLink]="['/inventories', inventoryId, 'products', product.productId]"
              title="Details"
            >
              <lord-icon
                src="https://cdn.lordicon.com/jnzhohhs.json"
                trigger="hover"
                style="width:32px;height:32px"
              >
              </lord-icon>
            </a>
          </td>
          <td>
            <a
              [routerLink]="['/inventories', inventoryId, 'products', product.productId, 'edit']"
              title="Edit"
            >
              <button class="add-bundle-button btn btn-warning">
                <lord-icon
                  src="https://cdn.lordicon.com/wkvacbiw.json"
                  trigger="hover"
                  style="width:32px;height:32px"
                >
                </lord-icon>
              </button>
            </a>
          </td>

          <td>
            <a
              *ngIf="!product.isTemporarilyDeleted"
              class="btn btn-danger"
              href="javascript:void(0)"
              (click)="deleteProduct(product); $event.stopPropagation()"
              title="Delete"
            >
              <lord-icon
                src="https://cdn.lordicon.com/skkahier.json"
                trigger="hover"
                style="width:32px;height:32px"
              >
              </lord-icon>
            </a>
            <a
              *ngIf="product.isTemporarilyDeleted"
              class="btn btn-info"
              href="javascript:void(0)"
              (click)="undoDelete(product); $event.stopPropagation()"
            >
              Restore
            </a>
          </td>

          <td></td>
        </tr>
      </tbody>
    </table>

    <div class="d-flex justify-content-center pb-3">
      <nav aria-label="Page navigation example">
        <ul class="pagination">
          <li class="page-item">
            <a class="page-link text-dark" aria-label="Previous" (click)="previousPage()">
              <span aria-hidden="true">&laquo;</span>
            </a>
          </li>
          <li class="page-item">
            <a class="page-link text-dark" disabled>Page-{{ actualCurrentPageShown }}</a>
          </li>
          <li class="page-item">
            <a class="page-link text-dark" aria-label="Next" (click)="nextPage()">
              <span aria-hidden="true">&raquo;</span>
            </a>
          </li>
        </ul>
      </nav>
    </div>

    <div
      id="notification"
      style="display: none; position: fixed; bottom: 10px; right: 10px; background-color: #4CAF50; color: white; padding: 10px; border-radius: 5px;"
    >
      Notification Text Here
    </div>

    <a [routerLink]="['/inventories', inventoryId, 'products', 'new']">
      <button class="add-bundle-button btn btn-success">Add Product</button>
    </a>
    <a [routerLink]="['/inventories']">
      <button class="btn btn-success">Inventory List</button>
    </a>
    <a (click)="deleteAllProducts()">
      <button class="delete-bundle-button btn btn-success">Delete All Products</button>
    </a>
  `,
  styles: [
    `
      .btn:hover {
        transform: translateY(2px);
        box-shadow: 0 0 rgba(0, 0, 0, 2);
        border-bottom-width: 1px;
      }

      .table-striped tbody tr:hover {
        background-color: #d8d8d8;
      }
    `,
  ],
})
export class InventoriesProductListComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private inventoryApi = inject(InventoryApiService);

  inventoryId: string = '';
  inventory: Inventory | null = null;
  inventoryProductList: InventoryProduct[] = [];

  // Search parameters
  productName: string = '';
  productQuantity: string = '';
  productPrice: string = '';
  productSalePrice: string = '';

  // Pagination
  currentPage: number = 0;
  pageSize: number = 15;
  actualCurrentPageShown: number = 1;
  totalItems: number = 0;
  totalPages: number = 0;

  // Search state
  lastParams = {
    productName: '',
    productQuantity: '',
    productPrice: '',
    productSalePrice: '',
  };

  ngOnInit(): void {
    this.inventoryId = this.route.snapshot.paramMap.get('id') || '';
    this.loadInventory();
    this.fetchProductList();
  }

  private loadInventory(): void {
    this.inventoryApi.getInventoryById(this.inventoryId).subscribe({
      next: inventory => {
        this.inventory = inventory;
      },
      error: () => {},
    });
  }

  private fetchProductList(): void {
    this.inventoryApi.getInventoryProducts(this.inventoryId).subscribe({
      next: products => {
        this.inventoryProductList = products.map(product => ({
          ...product,
          productPrice: parseFloat(product.productPrice.toFixed(2)),
          productSalePrice: parseFloat((product.productSalePrice || 0).toFixed(2)),
        }));
        this.loadTotalItem();
      },
      error: () => {
        this.inventoryProductList = [];
      },
    });
  }

  searchProduct(): void {
    const queryParams: Record<string, unknown> = {};

    if (this.productName && this.productName !== '') {
      queryParams.productName = this.productName;
      this.lastParams.productName = this.productName;
    }

    if (this.productQuantity && this.productQuantity !== '') {
      queryParams.productQuantity = this.productQuantity;
      this.lastParams.productQuantity = this.productQuantity;
    }

    if (this.productPrice && this.productPrice !== '') {
      queryParams.productPrice = this.productPrice;
      this.lastParams.productPrice = this.productPrice;
    }

    if (this.productSalePrice && this.productSalePrice !== '') {
      queryParams.productSalePrice = this.productSalePrice;
      this.lastParams.productSalePrice = this.productSalePrice;
    }

    this.inventoryApi.getInventoryProducts(this.inventoryId).subscribe({
      next: products => {
        let filteredProducts = products;

        // Apply filters
        if (queryParams.productName) {
          filteredProducts = filteredProducts.filter(p =>
            p.productName.toLowerCase().includes(queryParams.productName.toLowerCase())
          );
        }
        if (queryParams.productQuantity) {
          filteredProducts = filteredProducts.filter(p =>
            p.productQuantity.toString().includes(queryParams.productQuantity)
          );
        }
        if (queryParams.productPrice) {
          filteredProducts = filteredProducts.filter(p =>
            p.productPrice.toString().includes(queryParams.productPrice)
          );
        }
        if (queryParams.productSalePrice) {
          filteredProducts = filteredProducts.filter(p =>
            (p.productSalePrice || 0).toString().includes(queryParams.productSalePrice)
          );
        }

        this.inventoryProductList = filteredProducts.map(product => ({
          ...product,
          productPrice: parseFloat(product.productPrice.toFixed(2)),
          productSalePrice: parseFloat((product.productSalePrice || 0).toFixed(2)),
        }));

        this.loadTotalItem();
      },
      error: error => {
        if (error.status === 404) {
          this.inventoryProductList = [];
          this.currentPage = 0;
          this.updateActualCurrentPageShown();
        } else {
          alert('An error occurred: ' + error.statusText);
        }
      },
    });
  }

  clearQueries(): void {
    this.lastParams.productName = '';
    this.lastParams.productQuantity = '';
    this.lastParams.productPrice = '';
    this.lastParams.productSalePrice = '';

    this.productName = '';
    this.productQuantity = '';
    this.productPrice = '';
    this.productSalePrice = '';
    this.searchProduct();
  }

  deleteProduct(product: InventoryProduct): void {
    const ifConfirmed = confirm('Are you sure you want to remove this product?');
    if (ifConfirmed) {
      product.isTemporarilyDeleted = true;

      setTimeout(() => {
        if (product.isTemporarilyDeleted) {
          this.proceedToDelete(product);
        }
      }, 5000);
    }
  }

  undoDelete(product: InventoryProduct): void {
    product.isTemporarilyDeleted = false;
  }

  private proceedToDelete(product: InventoryProduct): void {
    if (!product.isTemporarilyDeleted) return;

    this.inventoryApi.deleteInventoryProduct(this.inventoryId, product.productId).subscribe({
      next: () => {
        this.showNotification(product.productName + ' has been deleted successfully!');
        setTimeout(() => {
          location.reload();
        }, 1000);
      },
      error: error => {
        alert(error.data?.errors || 'Data is inaccessible.');
      },
    });
  }

  deleteAllProducts(): void {
    const varIsConf = confirm('Are you sure you want to delete all products for this inventory?');
    if (varIsConf) {
      // Note: This would need a deleteAllProducts method in the API service
      alert('Delete all products functionality would be implemented here');
    }
  }

  nextPage(): void {
    if (this.currentPage + 1 < this.totalPages) {
      this.currentPage = this.currentPage + 1;
      this.updateActualCurrentPageShown();
      this.fetchProductList();
    }
  }

  previousPage(): void {
    if (this.currentPage - 1 >= 0) {
      this.currentPage = this.currentPage - 1;
      this.updateActualCurrentPageShown();
      this.fetchProductList();
    }
  }

  private updateActualCurrentPageShown(): void {
    this.actualCurrentPageShown = this.currentPage + 1;
  }

  private loadTotalItem(): void {
    // This would typically call an API to get the total count
    // For now, we'll use the length of the current list
    this.totalItems = this.inventoryProductList.length;
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
  }

  private showNotification(message: string): void {
    const notificationElement = document.getElementById('notification');
    if (notificationElement) {
      notificationElement.innerHTML = message;
      notificationElement.style.display = 'block';

      setTimeout(() => {
        notificationElement.style.display = 'none';
      }, 5000);
    }
  }
}
