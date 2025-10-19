import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryApiService } from '../../api/inventory-api.service';
import { InventoryProduct } from '../../models/inventory.model';

@Component({
  selector: 'app-inventories-product-update-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bColor text-center">
      <h2 class="titleProduct form" id="title">Update Product</h2>
    </div>
    <div class="p-3 formColor m-0" *ngIf="product">
      <div id="inventoryProductUpdateForm" class="form-horizontal" enctype="multipart/form-data">
        <div class="row">
          <div class="col-sm-6 form-group">
            <label class="control-label" for="item">Product</label>
            <input class="form-control" id="item" [(ngModel)]="product.productName" name="productName" type="text" placeholder="Please enter product name." />
            <input type="hidden" [(ngModel)]="product.inventoryId" [value]="inventoryId">
          </div>
        </div>
        <div class="col-sm-6 form-group">
          <label class="control-label" for="description">Description</label>
          <input class="form-control col-sm-4" id="description" [(ngModel)]="product.productDescription" name="productDescription" type="text" placeholder="Please enter product description." />
        </div>
        <div class="row">
          <div class="form-group col-sm-12">
            <label class="control-label" for="price">Price</label>
            <input class="form-control" id="price" [(ngModel)]="product.productPrice" name="productPrice" type="number" placeholder="Please enter a price" />
          </div>
        </div>
        <div class="form-group p-3">
          <div class="bundle marg col-sm-12">
            <label class="control-label" for="quantity">Quantity</label>
            <input class="form-control" id="quantity" [(ngModel)]="product.productQuantity" name="productQuantity" type="number" required title="Please enter quantities." />
          </div>
        </div>
        <div class="form-group p-3">
          <div class="bundle marg col-sm-12">
            <label class="control-label" for="salePrice">Sale Price</label>
            <input class="form-control" id="salePrice" [(ngModel)]="product.productSalePrice" name="productSalePrice" type="number" required title="Please enter the sale price." />
          </div>
        </div>
        <div class="form-group formColor">
          <div class="owner marg col-sm-6">
            <button id="newBtn" class="btn btn-default" type="button" (click)="submitProductUpdateForm()">Submit</button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class InventoriesProductUpdateFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private inventoryApi = inject(InventoryApiService);

  inventoryId: string = '';
  productId: string = '';
  product: InventoryProduct | null = null;
  method: string = 'edit';

  ngOnInit(): void {
    this.inventoryId = this.route.snapshot.paramMap.get('id') || '';
    this.productId = this.route.snapshot.paramMap.get('productId') || '';
    this.loadProduct();
  }

  private loadProduct(): void {
    this.inventoryApi.getInventoryProduct(this.inventoryId, this.productId).subscribe({
      next: (product) => {
        this.product = product;
      },
      error: (error) => this.handleHttpError(error)
    });
  }

  submitProductUpdateForm(): void {
    if (!this.product) return;

    if (!this.inventoryId) {
      alert("Inventory ID is missing.");
      return;
    }
    if (!this.productId) {
      alert("Product ID is missing.");
      return;
    }

    const data = {
      productName: this.product.productName,
      productDescription: this.product.productDescription,
      productPrice: this.product.productPrice,
      productQuantity: this.product.productQuantity,
      productSalePrice: this.product.productSalePrice || 0
    };

    this.inventoryApi.updateInventoryProduct(this.inventoryId, this.productId, data).subscribe({
      next: () => {
        this.router.navigate(['/inventories', this.inventoryId, 'products']);
      },
      error: (error) => this.handleHttpError(error)
    });
  }

  private handleHttpError(response: any): void {
    try { 
    } catch (e) {}

    let data = response && response.data;
    const status = response && response.status;
    const statusText = (response && response.statusText) || '';

    // Normalize string bodies (plain text or JSON-as-string)
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

    // Possible arrays the backend may use
    const errorsArr = Array.isArray(data.errors) ? data.errors : [];
    const detailsArr = Array.isArray(data.details) ? data.details : [];
    const violations = Array.isArray(data.violations || data.constraintViolations)
      ? (data.violations || data.constraintViolations) : [];

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
      .concat(violations.map(mapErr))
      .filter(Boolean)
      .join('\r\n');

    const baseMsg =
      data.message ||
      data.error_description ||
      data.errorMessage ||
      data.error ||
      data.title ||
      data.detail ||
      (typeof data === 'object' && Object.keys(data).length ? JSON.stringify(data) : '') ||
      (status ? ('HTTP ' + status + (statusText ? (' ' + statusText) : '')) : 'Request failed');

    alert(fieldText ? (baseMsg + '\r\n' + fieldText) : baseMsg);
  }
}


