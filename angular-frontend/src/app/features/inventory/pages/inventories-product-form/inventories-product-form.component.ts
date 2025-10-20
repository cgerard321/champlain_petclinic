import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryApiService } from '../../api/inventory-api.service';
import { ProductRequest } from '../../models/inventory.model';

@Component({
  selector: 'app-inventories-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bColor text-center">
      <h2 class="titleProduct form" id="title">New Product</h2>
    </div>
    <div class="p-3 formColor m-0">
      <div id="productForm" class="form-horizontal" enctype="multipart/form-data">
        <div class="row">
          <div class="col-sm-6 form-group">
            <label class="control-label" for="item">Product</label>
            <input
              class="form-control"
              id="item"
              [(ngModel)]="product.productName"
              name="productName"
              type="text"
              placeholder="Please enter product name."
            />
            <input type="hidden" [(ngModel)]="product.inventoryId" [value]="inventoryId" />
          </div>
        </div>
        <div class="col-sm-6 form-group">
          <label class="control-label" for="description">Description</label>
          <input
            class="form-control col-sm-4"
            id="description"
            [(ngModel)]="product.productDescription"
            name="productDescription"
            type="text"
            placeholder="Please enter product description."
          />
        </div>
        <div class="row">
          <div class="form-group col-sm-12">
            <label class="control-label" for="price">Price</label>
            <input
              class="form-control"
              id="price"
              step="0.01"
              min="0"
              [(ngModel)]="product.productPrice"
              name="productPrice"
              type="number"
              placeholder="Please enter a price"
            />
          </div>
        </div>
        <div class="form-group p-3">
          <div class="bundle marg col-sm-12">
            <label class="control-label" for="quantity">Quantity</label>
            <input
              class="form-control"
              id="quantity"
              min="0"
              [(ngModel)]="product.productQuantity"
              name="productQuantity"
              type="number"
              required
              title="Please enter quantities."
            />
          </div>
        </div>
        <div class="form-group p-3">
          <div class="bundle marg col-sm-12">
            <label class="control-label" for="salePrice">Sale Price</label>
            <input
              class="form-control"
              id="salePrice"
              step="0.01"
              min="0"
              [(ngModel)]="product.productSalePrice"
              name="productSalePrice"
              type="number"
              required
              title="Please enter the sale price."
            />
          </div>
        </div>
        <div class="form-group formColor">
          <div class="owner marg col-sm-6">
            <button id="newBtn" class="btn btn-default" type="button" (click)="submitProductForm()">
              Submit
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class InventoriesProductFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private inventoryApi = inject(InventoryApiService);

  inventoryId: string = '';
  product: ProductRequest = {
    productName: '',
    productDescription: '',
    productPrice: 0,
    productQuantity: 0,
    inventoryId: '',
  };

  ngOnInit(): void {
    this.inventoryId = this.route.snapshot.paramMap.get('id') || '';
    this.product.inventoryId = this.inventoryId;
  }

  submitProductForm(): void {
    const data = {
      productName: this.product.productName,
      productDescription: this.product.productDescription,
      productPrice: this.product.productPrice,
      productQuantity: this.product.productQuantity,
      productSalePrice: this.product.productSalePrice || 0,
    };

    this.inventoryApi.addProductToInventory(this.inventoryId, data).subscribe({
      next: () => {
        this.router.navigate(['/inventories', this.inventoryId, 'products']);
      },
      error: response => {
        this.handleHttpError(response);
      },
    });
  }

  private handleHttpError(response: { data?: unknown }): void {
    const data = (response && response.data) || {};
    const baseMsg =
      (typeof data === 'string' && data) ||
      (data as any).message ||
      (data as any).error ||
      (response && (response as any).status
        ? 'HTTP ' + (response as any).status + ' ' + ((response as any).statusText || '')
        : 'Request failed');

    const fieldErrors =
      (Array.isArray((data as any).errors) && (data as any).errors) ||
      (Array.isArray((data as any).details) && (data as any).details) ||
      (data as any).fieldErrors ||
      [];

    let fieldText = '';
    if (Array.isArray(fieldErrors) && fieldErrors.length) {
      fieldText = fieldErrors
        .map(
          (e: {
            field?: string;
            path?: string;
            parameter?: string;
            defaultMessage?: string;
            message?: string;
          }) => {
            if (typeof e === 'string') return e;
            const field = e.field || e.path || e.parameter || '';
            const msg = e.defaultMessage || e.message || (e as any).reason || JSON.stringify(e);
            return field ? field + ': ' + msg : msg;
          }
        )
        .join('\r\n');
    }

    alert(fieldText ? baseMsg + '\r\n' + fieldText : baseMsg);
  }
}
