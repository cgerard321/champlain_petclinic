import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { DeliveryType, ProductStatus } from '@features/prod/models/product.model';
import { ImageService } from '@features/prod/services/image.service';
import { ProductService } from '@features/prod/services/product.service';

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
              productType: 'FOOD',
              productTypeId: '586d0700-57db-4312-b6f1-413b79dd018c',
              productStatus: ProductStatus.AVAILABLE,
              deliveryType: DeliveryType.DELIVERY,
            },
          },
        },
        { provide: MatDialogRef, useValue: { close: () => undefined } },
        {
          provide: ProductService,
          useValue: {
            getProductEnums: () =>
              of({
                productType: [
                  { productTypeId: '586d0700-57db-4312-b6f1-413b79dd018c', typeName: 'FOOD' },
                ],
                productStatus: [],
                deliveryType: ['DELIVERY', 'PICKUP'],
              }),
            updateProduct: () => of({ productId: 'product-1' }),
            updateProductImage: () => of({ productId: 'product-1' }),
          },
        },
        {
          provide: ImageService,
          useValue: {
            toFileDetails: () =>
              Promise.resolve({
                fileName: 'product.png',
                fileType: 'image/png',
                fileData: 'aW1hZ2U=',
              }),
          },
        },
      ],
    }).compileComponents();
  });

  it('creates the Update dialog', () => {
    const fixture = TestBed.createComponent(ProductUpdateDialog);
    expect(fixture.componentInstance).toBeTruthy();
  });
});

  /*it('loads delivery types from the enums API', () => {
    const fixture = TestBed.createComponent(ProductUpdateDialog);
    fixture.detectChanges();

    const component = fixture.componentInstance as any;

    expect(component.deliveryTypes()).toEqual(['DELIVERY', 'PICKUP']);
  });*/
