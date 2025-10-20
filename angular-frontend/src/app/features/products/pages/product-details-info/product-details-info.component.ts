import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProductApiService } from '../../api/product-api.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-details-info',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <style>
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

    <div class="container d-flex justify-content-center mt-5">
      <div class="card shadow-sm p-4" style="max-width: 600px; width: 100%;">
        <h4 class="mb-5 text-center">Products Details Infos</h4>

        <div class="p-3 formColor m-0" *ngIf="product">
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Image:</div>
            <img
              class="col-md-2 custom-value"
              *ngIf="product.imageData"
              [src]="'data:' + product.imageType + ';base64,' + product.imageData"
              [alt]="product.productName"
              width="120"
            />
          </div>
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Name:</div>
            <div class="col-md-2 custom-value">{{ product.productName }}</div>
          </div>
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Id:</div>
            <div class="col-md-2 custom-value">{{ product.productId }}</div>
          </div>

          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Description:</div>
            <div class="col-md-2 custom-value">{{ product.productDescription }}</div>
          </div>

          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product SalePrice:</div>
            <div class="col-md-2 custom-value">
              {{ product.productSalePrice | currency: '$' : 2 }}
            </div>
          </div>

          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Quantity:</div>
            <div class="col-md-2 custom-value">{{ product.productQuantity }}</div>
          </div>
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Rating:</div>
            <div class="col-md-2 custom-value">{{ product.averageRating }}</div>
          </div>
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Type:</div>
            <div class="col-md-2 custom-value">{{ product.productType }}</div>
          </div>
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Delivery Type:</div>
            <div class="col-md-2 custom-value">{{ product.deliveryType }}</div>
          </div>
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product Status:</div>
            <div class="col-md-2 custom-value">{{ product.productStatus }}</div>
          </div>
          <div class="row custom-row">
            <div class="col-md-5 custom-label">Product isUnlisted:</div>
            <div class="col-md-2 custom-value">{{ product.isUnlisted }}</div>
          </div>
        </div>

        <div class="text-center mt-5">
          <a class="btn btn-primary" routerLink="/products"> Back to Products page </a>
        </div>
      </div>
    </div>
  `,
})
export class ProductDetailsInfoComponent implements OnInit {
  private productApi = inject(ProductApiService);
  private route = inject(ActivatedRoute);

  product: Product | null = null;
  private productId: string = '';

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('productId') || '';

    if (!this.productId) {
      return;
    }

    this.loadProduct();
  }

  private loadProduct(): void {
    this.productApi.getProductById(this.productId).subscribe({
      next: product => {
        this.product = product;
        this.fetchImage();
      },
      error: () => {},
    });
  }

  private fetchImage(): void {
    if (this.product && this.product.imageId) {
      this.productApi.getProductImage(this.product.imageId).subscribe({
        next: imageResp => {
          if (imageResp === '') {
            return;
          }
          if (this.product) {
            this.product.imageData = imageResp.imageData;
            this.product.imageType = imageResp.imageType;
          }
        },
        error: () => {},
      });
    }
  }
}
