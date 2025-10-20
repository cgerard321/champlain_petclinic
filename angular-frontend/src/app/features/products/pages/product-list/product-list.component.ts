import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductApiService } from '../../api/product-api.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <style>
      .btn:hover {
        transform: translateY(2px);
        box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.2);
        border-bottom-width: 1px;
      }

      .table-striped tbody tr:hover {
        background-color: #d8d8d8;
      }

      .action-toast {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        max-width: 380px;
        background: #333;
        color: #fff;
        padding: 12px 14px;
        border-radius: 8px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
        z-index: 9999;
        display: none;
      }

      .action-toast.show {
        display: block;
      }
    </style>

    <h2>Products</h2>

    <table class="table table-striped">
      <thead>
        <tr>
          <td>Image</td>
          <td>Name</td>
          <td>Id</td>
          <td>Quantity</td>
          <td>Description</td>
          <td>Sale Price</td>
          <td>Type</td>
          <td>Status</td>
          <td>Max Rating</td>
        </tr>
        <tr>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td>
            <span>
              <input
                type="number"
                step="0.01"
                style="width: 80%"
                min="0"
                [(ngModel)]="productSalePrice"
                (keyup.enter)="searchProduct()"
              />
            </span>
          </td>
          <td>
            <span>
              <select [(ngModel)]="productType" (keyup.enter)="searchProduct()">
                <option value="">-- Type --</option>
                <option *ngFor="let type of productTypeOptions" [value]="type">{{ type }}</option>
              </select>
            </span>
          </td>
          <td></td>
          <td>
            <span
              ><input
                type="number"
                min="0"
                style="width: 80%"
                [(ngModel)]="averageRating"
                (keyup.enter)="searchProduct()"
            /></span>
          </td>
          <td></td>
          <td>
            <button class="btn btn-success" type="button" (click)="clearQueries()" title="Clear">
              <lord-icon
                src="https://cdn.lordicon.com/zxvuvcnc.json"
                trigger="hover"
                style="width:32px;height:32px"
              >
              </lord-icon>
            </button>
          </td>
          <td>
            <button class="btn btn-success" type="button" (click)="searchProduct()" title="Search">
              <lord-icon
                src="https://cdn.lordicon.com/fkdzyfle.json"
                trigger="hover"
                style="width:32px;height:32px"
              >
              </lord-icon>
            </button>
          </td>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let product of productList">
          <td>
            <img
              *ngIf="product.imageData"
              [src]="'data:' + product.imageType + ';base64,' + product.imageData"
              [alt]="product.productName"
              width="90"
            />
          </td>
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
            <span>{{ product.productDescription }}</span>
          </td>
          <td>
            <span>{{ product.productSalePrice }}$</span>
          </td>
          <td>
            <span>{{ product.productType }}</span>
          </td>
          <td>
            <span>{{ product.productStatus }}</span>
          </td>
          <td>
            <span>{{ product.averageRating }}</span>
          </td>

          <td>
            <a class="btn btn-info" [routerLink]="['/products', product.productId]" title="Details">
              <lord-icon
                src="https://cdn.lordicon.com/jnzhohhs.json"
                trigger="hover"
                style="width:32px;height:32px"
              >
              </lord-icon>
            </a>
          </td>

          <td>
            <a [routerLink]="['/products', product.productId, 'edit']" title="Edit">
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

    <a [routerLink]="['/products/new']">
      <button class="add-bundle-button btn btn-success">Add Product</button>
    </a>

    <div class="action-toast" [class.show]="toast.visible">
      <div class="title">{{ toast.title }}</div>
      <div class="line" *ngFor="let line of toast.lines">{{ line }}</div>
      <div class="actions" *ngIf="toast.actions && toast.actions.length">
        <button
          class="btn"
          [class.btn-primary]="(action.kind || 'primary') === 'primary'"
          [class.btn-secondary]="action.kind === 'secondary'"
          [class.btn-danger]="action.kind === 'danger'"
          *ngFor="let action of toast.actions; let i = index"
          (click)="onToastAction(i)"
        >
          {{ action.label }}
        </button>
      </div>
    </div>
  `,
})
export class ProductListComponent implements OnInit {
  private productApi = inject(ProductApiService);

  productList: Product[] = [];
  productTypeOptions = ['FOOD', 'MEDICATION', 'ACCESSORY', 'EQUIPMENT'];

  productSalePrice: number | null = null;
  productType: string = '';
  averageRating: number | null = null;

  currentPage = 0;
  pageSize = 15;
  actualCurrentPageShown = 1;
  totalPages = 0;

  toast = { visible: false, title: '', lines: [] as string[], actions: [] as unknown[] };
  private toastTimer: unknown = null;

  ngOnInit(): void {
    this.fetchProductList();
  }

  private fetchProductList(): void {
    this.productApi
      .getAllProducts(
        undefined,
        this.productSalePrice || undefined,
        undefined,
        this.averageRating || undefined,
        undefined,
        this.productType || undefined
      )
      .subscribe({
        next: products => {
          this.productList = products.map(product => ({
            ...product,
            productSalePrice: parseFloat((product.productSalePrice || 0).toFixed(2)),
          }));
          this.fetchImages();
        },
        error: error => {
          if (error.status === 404) {
            this.currentPage = 0;
            this.updateActualCurrentPageShown();
          } else {
            alert('An error occurred: ' + error.statusText);
          }
        },
      });
  }

  private fetchImages(): void {
    this.productList.forEach(product => {
      if (product.imageId) {
        this.productApi.getProductImage(product.imageId).subscribe({
          next: imageResp => {
            if (imageResp === 0) {
              return;
            }
            product.imageData = imageResp.imageData;
            product.imageType = imageResp.imageType;
          },
          error: () => {},
        });
      }
    });
  }

  searchProduct(): void {
    this.resetDefaultValues();
    this.fetchProductList();
  }

  clearQueries(): void {
    this.productSalePrice = null;
    this.productType = '';
    this.averageRating = null;
    this.searchProduct();
  }

  private resetDefaultValues(): void {
    this.currentPage = 0;
    this.pageSize = 15;
    this.actualCurrentPageShown = 1;
  }

  private updateActualCurrentPageShown(): void {
    this.actualCurrentPageShown = this.currentPage + 1;
  }

  nextPage(): void {
    if (this.currentPage + 1 < this.totalPages) {
      this.currentPage++;
      this.updateActualCurrentPageShown();
      this.fetchProductList();
    }
  }

  previousPage(): void {
    if (this.currentPage - 1 >= 0) {
      this.currentPage--;
      this.updateActualCurrentPageShown();
      this.fetchProductList();
    }
  }

  deleteProduct(product: Product): void {
    this.toastShow({
      title: 'Delete product?',
      lines: ['Are you sure you want to delete "' + product.productName + '"?'],
      actions: [
        { label: 'Delete', kind: 'danger', onClick: () => this.scheduleSoftDelete(product) },
        { label: 'Cancel', kind: 'secondary', onClick: () => {} },
      ],
    });
  }

  private scheduleSoftDelete(product: Product): void {
    product.isTemporarilyDeleted = true;

    this.toastShow({
      title: 'Deleting in 5 seconds…',
      lines: [product.productName],
      actions: [
        {
          label: 'Undo',
          kind: 'secondary',
          onClick: () => {
            product.isTemporarilyDeleted = false;
          },
        },
      ],
    });

    setTimeout(() => {
      if (!product.isTemporarilyDeleted) return;

      this.productApi.deleteProduct(product.productId, false).subscribe({
        next: () => {
          this.toastShow({ title: 'Success', lines: ['Product deleted.'], autoHideMs: 2000 });
          setTimeout(() => {
            location.reload();
          }, 800);
        },
        error: err => {
          if (err.status === 409) {
            this.toastShow({
              title: 'Product is part of a bundle',
              lines: ['Deleting will also delete its bundle(s). Continue?'],
              actions: [
                {
                  label: 'Delete all',
                  kind: 'danger',
                  onClick: () => {
                    this.productApi.deleteProduct(product.productId, true).subscribe({
                      next: () => {
                        this.toastShow({
                          title: 'Success',
                          lines: ['Product and bundle(s) deleted.'],
                          autoHideMs: 2000,
                        });
                        setTimeout(() => {
                          location.reload();
                        }, 800);
                      },
                      error: err2 => {
                        product.isTemporarilyDeleted = false;
                        const msg =
                          (err2.data && err2.data.message) || err2.statusText || 'Delete failed';
                        this.toastShow({ title: 'Error', lines: [msg], autoHideMs: 3000 });
                      },
                    });
                  },
                },
                {
                  label: 'Cancel',
                  kind: 'secondary',
                  onClick: () => {
                    product.isTemporarilyDeleted = false;
                  },
                },
              ],
            });
          } else {
            product.isTemporarilyDeleted = false;
            const msg = (err.data && err.data.message) || err.statusText || 'Delete failed';
            this.toastShow({ title: 'Error', lines: [msg], autoHideMs: 3000 });
          }
        },
      });
    }, 5000);
  }

  undoDelete(product: Product): void {
    product.isTemporarilyDeleted = false;
  }

  private toastShow({
    title = '',
    lines = [],
    actions = [],
    autoHideMs = 0,
  }: {
    title?: string;
    lines?: string[];
    actions?: unknown[];
    autoHideMs?: number;
  }): void {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
      this.toastTimer = null;
    }
    this.toast.title = title;
    this.toast.lines = lines;
    this.toast.actions = actions;
    this.toast.visible = true;
    if (!actions.length && autoHideMs > 0) {
      this.toastTimer = setTimeout(() => this.toastHide(), autoHideMs);
    }
  }

  private toastHide(): void {
    this.toast.visible = false;
    this.toast.title = '';
    this.toast.lines = [];
    this.toast.actions = [];
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
      this.toastTimer = null;
    }
  }

  onToastAction(index: number): void {
    try {
      const action = this.toast.actions[index];
      this.toastHide();
      if (action && typeof action.onClick === 'function') action.onClick();
    } catch (e) {}
  }
}
