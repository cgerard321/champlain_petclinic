import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import {
  DeliveryType,
  Product,
  ProductStatus,
  ProductType,
} from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';

import { Prod } from './prod';

describe('Prod', () => {
  const product: Product = {
    productId: 'product-1',
    productName: 'Dog food',
    productDescription: 'Food',
    productSalePrice: 10,
    productQuantity: 5,
    isUnlisted: false,
    productType: ProductType.FOOD,
    productStatus: ProductStatus.AVAILABLE,
    deliveryType: DeliveryType.DELIVERY,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Prod],
      providers: [
        { provide: ProductService, useValue: { getProducts: () => of([product]) } },
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of(undefined) }) } },
      ],
    }).compileComponents();
  });

  it('creates the Product page', () => {
    const fixture = TestBed.createComponent(Prod);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('loads products on initialization', () => {
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Dog food');
  });
});
