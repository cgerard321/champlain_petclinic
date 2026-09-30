import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { vi } from 'vitest';

import {
  DeliveryType,
  Product,
  ProductStatus,
  ProductType,
} from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';

import { Prod } from './prod';

describe('Prod', () => {
  let getProducts: ReturnType<typeof vi.fn>;
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
    getProducts = vi.fn().mockReturnValue(of([product]));
    await TestBed.configureTestingModule({
      imports: [Prod],
      providers: [
        { provide: ProductService, useValue: { getProducts } },
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

  it('searches by product name after the debounce period', fakeAsync(() => {
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();
    getProducts.mockClear();

    const input = fixture.nativeElement.querySelector('input[type="search"]');
    input.value = 'horse';
    input.dispatchEvent(new Event('input'));

    tick(299);
    expect(getProducts).not.toHaveBeenCalled();

    tick(1);
    expect(getProducts).toHaveBeenCalledWith({ productName: 'horse' });

    fixture.componentInstance['loadProducts']();
    expect(getProducts).toHaveBeenLastCalledWith({ productName: 'horse' });
  }));
});
