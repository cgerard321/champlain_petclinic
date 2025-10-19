import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductApiService } from '../../api/product-api.service';
import { Product, ProductRequest } from '../../models/product.model';

@Component({
  selector: 'app-product-update-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container d-flex justify-content-center mt-5">
      <div class="card shadow-sm p-4" style="max-width: 600px; width: 100%;">
        <h4 class="mb-4 text-center">Update Product</h4>
        <div class="p-3 formColor m-0">
          <div id="productUpdateForm" class="form-horizontal">
            <div class="row">
              <div class="form-group mb-3">
                <img *ngIf="product.imageData"
                     [src]="'data:' + product.imageType + ';base64,' + product.imageData"
                     [alt]="product.productName"
                     width="120">
              </div>
              <div class="form-group mb-3">
                <label class="control-label" for="item">Product Name</label>
                <input class="form-control" id="item" 
                       [(ngModel)]="product.productName" 
                       name="productName" 
                       type="text" 
                       placeholder="Please enter product name." 
                       required 
                       title="Please enter product name." />
              </div>
            </div>

            <div class="form-group mb-3">
              <label class="control-label" for="description">Description</label>
              <input class="form-control col-sm-4" id="description" 
                     [(ngModel)]="product.productDescription" 
                     name="productDescription" 
                     type="text" 
                     placeholder="Please enter product description." 
                     required 
                     title="Please enter product description."/>
            </div>

            <div class="form-group mb-3">
              <div class="bundle marg col-sm-12">
                <label class="control-label" for="quantity">Quantity</label>
                <input class="form-control" id="quantity" 
                       [(ngModel)]="product.productQuantity" 
                       name="productQuantity" 
                       type="number" 
                       required 
                       title="Please enter quantities." />
              </div>
            </div>

            <div class="form-group mb-3">
              <div class="bundle marg col-sm-12">
                <label class="control-label" for="salePrice">Sale Price</label>
                <input class="form-control" id="salePrice" 
                       step="0.01" 
                       min="0.01" 
                       [(ngModel)]="product.productSalePrice" 
                       name="productSalePrice" 
                       type="number" 
                       required 
                       title="Please enter the sale price." />
              </div>
            </div>

            <div class="form-group mb-3">
              <div class="bundle marg col-sm-12">
                <label class="control-label" for="type">Type</label>
                <select class="form-control" id="type" 
                        [(ngModel)]="product.productType" 
                        name="productType" 
                        required>
                  <option value="">-- Select Product Type --</option>
                  <option *ngFor="let type of productTypeOptions" [value]="type">{{type}}</option>
                </select>
              </div>
            </div>

            <div class="form-group mb-3">
              <div class="bundle marg col-sm-12">
                <label class="control-label" for="image">Image Id</label>
                <input class="form-control" id="image" 
                       [(ngModel)]="product.imageId" 
                       name="imageId" 
                       type="text" 
                       placeholder="Please enter the image Id." />
              </div>
            </div>

            <div class="form-group mb-3">
              <div class="bundle marg col-sm-12">
                <label class="control-label" for="productStatus">Product Status</label>
                <select class="form-control" id="productStatus" 
                        [(ngModel)]="product.productStatus" 
                        name="productStatus" 
                        required>
                  <option value="">-- Select Product Status --</option>
                  <option *ngFor="let status of productStatusOptions" [value]="status">{{status}}</option>
                </select>
              </div>
            </div>

            <div class="form-group mb-3">
              <div class="bundle marg col-sm-12">
                <label class="control-label" for="deliveryType">Delivery Type</label>
                <select class="form-control" id="deliveryType" 
                        [(ngModel)]="product.deliveryType" 
                        name="deliveryType" 
                        required>
                  <option value="">-- Select Delivery Type --</option>
                  <option *ngFor="let type of deliveryTypeOptions" [value]="type">{{type}}</option>
                </select>
              </div>
            </div>

            <div class="form-group mb-3">
              <div class="bundle marg col-sm-12">
                <label class="control-label" for="isUnlisted">isUnlisted</label>
                <select class="form-control" id="isUnlisted" 
                        [(ngModel)]="product.isUnlisted" 
                        name="isUnlisted" 
                        required>
                  <option [value]="false">false</option>
                  <option [value]="true">true</option>
                </select>
              </div>
            </div>

            <div class="text-center">
              <div class="bundle marg col-sm-12">
                <div class="owner marg col-sm-6">
                  <button id="newBtn" class="btn btn-primary px-4" type="button" (click)="submitProductUpdateForm()">Submit</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ProductUpdateFormComponent implements OnInit {
  private productApi = inject(ProductApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  product: Product = {
    productId: '',
    productName: '',
    productDescription: '',
    productSalePrice: 0,
    productQuantity: 0,
    productType: 'ACCESSORY',
    productStatus: 'AVAILABLE',
    averageRating: 0,
    imageId: '',
    requestCount: 0,
    isUnlisted: false,
    dateAdded: new Date(),
    deliveryType: 'NO_DELIVERY_OPTION'
  };

  productTypeOptions = ["FOOD", "MEDICATION", "ACCESSORY", "EQUIPMENT"];
  productStatusOptions = ["AVAILABLE", "PRE_ORDER", "OUT_OF_STOCK"];
  deliveryTypeOptions = ["DELIVERY", "PICKUP", "DELIVERY_AND_PICKUP", "NO_DELIVERY_OPTION"];

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
      next: (product) => {
        this.product = product;
        if (this.product.isUnlisted === undefined || this.product.isUnlisted === null) {
          this.product.isUnlisted = false;
        }
        this.fetchImage();
      },
      error: () => {
      }
    });
  }

  private fetchImage(): void {
    if (this.product.imageId) {
      this.productApi.getProductImage(this.product.imageId).subscribe({
        next: (imageResp) => {
          if (imageResp === "") {
            return;
          }
          this.product.imageData = imageResp.imageData;
          this.product.imageType = imageResp.imageType;
        },
        error: () => {
        }
      });
    }
  }

  submitProductUpdateForm(): void {
    const data: ProductRequest = {
      productName: this.product.productName,
      productDescription: this.product.productDescription,
      productSalePrice: this.product.productSalePrice,
      productQuantity: this.product.productQuantity,
      productType: this.product.productType,
      productStatus: this.product.productStatus,
      deliveryType: this.product.deliveryType,
      imageId: this.product.imageId,
      isUnlisted: this.product.isUnlisted
    };

    this.productApi.updateProduct(this.productId, data).subscribe({
      next: () => {
        this.router.navigate(['/products']);
      },
      error: (response) => {
        const error = response.error;
        error.errors = error.errors || [];
        const errorMessage = error.error + "\r\n" + error.errors.map((e: any) => {
          return e.field + ": " + e.defaultMessage;
        }).join("\r\n");
        alert(errorMessage);
      }
    });
  }
}