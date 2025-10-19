import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { InventoryApiService } from '../../api/inventory-api.service';
import { InventoryProduct, Inventory } from '../../models/inventory.model';

@Component({
  selector: 'app-inventories-product-details-info',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <link crossorigin="anonymous" href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css"
          integrity="sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
      .custom-card {
        text-align: center;
        background-color: #fff;
        border: 1px solid #dcdcdc;
        border-radius: 0.25rem;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        margin: 20px;
        padding: 20px;
        align-items: center;
      }

      .custom-label {
        font-weight: bold;
      }

      .custom-row {
        margin-bottom: 10px;
      }

      .custom-value {
        white-space: nowrap;
      }
    </style>

    <div class="bColor text-center">
      <h2 id="title">Product Details Info</h2>
    </div>

    <div class="card-mx-auto" *ngIf="!loading && product && inventory">
      <div class="custom-card">
        <div class="row custom-row">
          <div class="col-md-2 custom-label">Inventory Id:</div>
          <div class="col-md-2 custom-value">{{product.inventoryId}}</div>
        </div>
        <div class="row custom-row">
          <div class="col-md-2 custom-label">Inventory Code:</div>
          <div class="col-md-2 custom-value">{{inventory.inventoryCode}}</div>
        </div>

        <div class="row custom-row">
          <div class="col-md-2 custom-label">Product Id:</div>
          <div class="col-md-2 custom-value">{{product.productId}}</div>
        </div>

        <div class="row custom-row">
          <div class="col-md-2 custom-label">Product Name:</div>
          <div class="col-md-2 custom-value">{{product.productName}}</div>
        </div>

        <div class="row custom-row">
          <div class="col-md-2 custom-label">Product Description:</div>
          <div class="col-md-2 custom-value">{{product.productDescription}}</div>
        </div>

        <div class="row custom-row">
          <div class="col-md-2 custom-label">Product Price:</div>
          <div class="col-md-2 custom-value">{{product.productPrice | currency:"$":2}}</div>
        </div>

        <div class="row custom-row">
          <div class="col-md-2 custom-label">Product SalePrice:</div>
          <div class="col-md-2 custom-value">{{product.productSalePrice | currency:"$":2}}</div>
        </div>

        <div class="row custom-row">
          <div class="col-md-2 custom-label">Product Quantity:</div>
          <div class="col-md-2 custom-value">{{product.productQuantity}}</div>
        </div>
        
        <div class="row custom-row">
          <div class="col-md-2 custom-label">Product Last Updated:</div>
          <div class="col-md-2 custom-value">{{product.lastUpdated | date:'medium'}}</div>
        </div>

        <div class="text-center mt-4" *ngIf="!loading">
          <a class="btn btn-primary"
             [routerLink]="['/inventories', inventoryId, 'products']">
            Back to Inventory Products page
          </a>
        </div>
      </div>
    </div>

    <div *ngIf="loading" class="text-center">
      <p>Loading product details...</p>
    </div>

    <div *ngIf="error" class="text-center text-danger">
      <p>{{error}}</p>
    </div>
  `
})
export class InventoriesProductDetailsInfoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private inventoryApi = inject(InventoryApiService);

  inventoryId: string = '';
  productId: string = '';
  product: InventoryProduct | null = null;
  inventory: Inventory | null = null;
  loading: boolean = true;
  error: string | null = null;

  ngOnInit(): void {
    this.inventoryId = this.route.snapshot.paramMap.get('id') || '';
    this.productId = this.route.snapshot.paramMap.get('productId') || '';
    this.loadProduct();
    this.loadInventory();
  }

  private loadProduct(): void {
    this.inventoryApi.getInventoryProduct(this.inventoryId, this.productId).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Could not load product details.';
        this.loading = false;
      }
    });
  }

  private loadInventory(): void {
    this.inventoryApi.getInventoryById(this.inventoryId).subscribe({
      next: (inventory) => {
        this.inventory = inventory;
      },
      error: (err) => {
      }
    });
  }
}


