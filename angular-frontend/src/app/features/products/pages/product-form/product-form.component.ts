import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductApiService } from '../../api/product-api.service';
import { ProductRequest } from '../../models/product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container d-flex justify-content-center mt-5">
      <div class="card shadow-sm p-4" style="max-width: 600px; width: 100%;">
        <h4 class="mb-4 text-center">Add New Product</h4>
        <div class="p-3 formColor m-0">
          <div id="productForm" class="form-horizontal">

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

            <div class="form-group mb-3">
              <label class="control-label" for="description">Description</label>
              <input class="form-control" id="description" 
                     [(ngModel)]="product.productDescription" 
                     name="productDescription" 
                     type="text" 
                     placeholder="Please enter product description." 
                     required 
                     title="Please enter product description." />
            </div>

            <div class="form-group mb-3">
              <label class="control-label" for="quantity">Quantity</label>
              <input class="form-control" id="quantity" 
                     min="0" 
                     [(ngModel)]="product.productQuantity" 
                     name="productQuantity" 
                     type="number" 
                     required 
                     title="Please enter quantities." />
            </div>

            <div class="form-group mb-4">
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

            <div class="form-group mb-3">
              <label class="control-label" for="image">Image Id</label>
              <input class="form-control" id="image" 
                     [(ngModel)]="product.imageId" 
                     name="imageId" 
                     type="text" 
                     placeholder="Please enter the image Id." />
            </div>

            <div class="form-group mb-3">
              <label class="control-label" for="type">Product Type</label>
              <select class="form-control" id="type" 
                      [(ngModel)]="product.productType" 
                      name="productType" 
                      required>
                <option value="">-- Select Product Type --</option>
                <option *ngFor="let type of productTypeOptions" [value]="type">{{type}}</option>
              </select>
            </div>

            <div class="form-group mb-3">
              <label class="control-label" for="productStatus">Product Status</label>
              <select class="form-control" id="productStatus" 
                      [(ngModel)]="product.productStatus" 
                      name="productStatus" 
                      required>
                <option value="">-- Select Product Status --</option>
                <option *ngFor="let status of productStatusOptions" [value]="status">{{status}}</option>
              </select>
            </div>

            <div class="form-group mb-3">
              <label class="control-label" for="deliveryType">Delivery Type</label>
              <select class="form-control" id="deliveryType" 
                      [(ngModel)]="product.deliveryType" 
                      name="deliveryType" 
                      required>
                <option value="">-- Select Delivery Type --</option>
                <option *ngFor="let type of deliveryTypeOptions" [value]="type">{{type}}</option>
              </select>
            </div>

            <div class="form-group mb-3">
              <label class="control-label" for="isUnlisted">isUnlisted</label>
              <select class="form-control" id="isUnlisted" 
                      [(ngModel)]="product.isUnlisted" 
                      name="isUnlisted" 
                      required>
                <option [value]="false">false</option>
                <option [value]="true">true</option>
              </select>
            </div>

            <div class="text-center">
              <div class="owner marg col-sm-6">
                <button id="newBtn" class="btn btn-primary px-4" type="button" (click)="submitProductForm()">Submit</button>
              </div>
            </div>
            <br>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ProductFormComponent implements OnInit {
  private productApi = inject(ProductApiService);
  private router = inject(Router);

  product: ProductRequest = {
    productName: '',
    productDescription: '',
    productSalePrice: 0,
    productQuantity: 0,
    productType: 'ACCESSORY',
    productStatus: 'AVAILABLE',
    deliveryType: 'NO_DELIVERY_OPTION',
    imageId: '',
    isUnlisted: false
  };

  productTypeOptions = ["FOOD", "MEDICATION", "ACCESSORY", "EQUIPMENT"];
  productStatusOptions = ["AVAILABLE", "PRE_ORDER", "OUT_OF_STOCK"];
  deliveryTypeOptions = ["DELIVERY", "PICKUP", "DELIVERY_AND_PICKUP", "NO_DELIVERY_OPTION"];

  ngOnInit(): void {
    this.product.isUnlisted = false;
  }

  submitProductForm(): void {
    const data = {
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

    this.productApi.createProduct(data).subscribe({
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