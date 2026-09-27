import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { ImageService } from '@features/prod/services/image.service';
import { ProductService } from '@features/prod/services/product.service';
import { DeliveryType, ProductStatus, ProductType } from '@features/prod/models/product.model';
import { ProductUpdateDialog } from './product-update-dialog';

describe('ProductUpdateDialog', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductUpdateDialog],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            product: {
              productId: 'product-1',
              productName: 'Food',
              productDescription: 'Description',
              productSalePrice: 10,
              productQuantity: 5,
              isUnlisted: false,
              productType: ProductType.FOOD,
              productStatus: ProductStatus.AVAILABLE,
              deliveryType: DeliveryType.DELIVERY,
            },
          },
        },
        { provide: MatDialogRef, useValue: { close: () => undefined } },
        { provide: ProductService, useValue: { getProductEnums: () => of({ productType: [], productStatus: [], deliveryType: [] }) } },
        { provide: ImageService, useValue: { uploadImage: () => of({ imageId: 'image-1' }) } },
      ],
    }).compileComponents();
  });

  it('creates the Update dialog', () => {
    const fixture = TestBed.createComponent(ProductUpdateDialog);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
