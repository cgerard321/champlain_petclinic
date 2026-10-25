import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Bundle } from '@features/prod/models/bundle.model';
import { DeliveryType, Product, ProductStatus } from '@features/prod/models/product.model';
import { BundleService } from '@features/prod/services/bundle.service';

import { ProductBundleAddDialog } from './product-bundle-add-dialog';

describe('ProductBundleAddDialog', () => {
  const baseProduct: Product = {
    productId: 'product-1',
    productName: 'Dog food',
    productDescription: 'Food',
    productSalePrice: 10,
    productQuantity: 5,
    isUnlisted: false,
    productType: 'FOOD',
    productTypeId: '586d0700-57db-4312-b6f1-413b79dd018c',
    productStatus: ProductStatus.AVAILABLE,
    deliveryType: DeliveryType.DELIVERY,
  };
  const products: Product[] = [
    baseProduct,
    { ...baseProduct, productId: 'product-2', productName: 'Horse Saddle', productSalePrice: 20 },
  ];
  const createdBundle: Bundle = {
    bundleId: 'bundle-1',
    bundleName: 'Starter Kit',
    bundleDescription: 'Everything to get started',
    productIds: ['product-1', 'product-2'],
    originalTotalPrice: 30,
    bundlePrice: 25,
  };
  const numberEvent = (value: number): Event =>
    ({ target: { valueAsNumber: value } }) as unknown as Event;

  let fixture: ComponentFixture<ProductBundleAddDialog>;
  let component: ProductBundleAddDialog;
  let dialogRef: { close: ReturnType<typeof vi.fn> };
  let dialogOpen: ReturnType<typeof vi.fn>;
  let createBundle: ReturnType<typeof vi.fn>;

  const selectProducts = (...productIds: string[]): void => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(new Set(productIds)) });
    component['openProductSelector']();
  };

  const fillDetails = (): void => {
    component['model'].update((current) => ({
      ...current,
      bundleName: 'Starter Kit',
      bundleDescription: 'Everything to get started',
    }));
  };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };
    dialogOpen = vi.fn();
    createBundle = vi.fn().mockReturnValue(of(createdBundle));
    TestBed.configureTestingModule({
      imports: [ProductBundleAddDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { products } },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: BundleService, useValue: { createBundle } },
      ],
    });
    TestBed.overrideProvider(MatDialog, { useValue: { open: dialogOpen } });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(ProductBundleAddDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates the Add Bundle dialog', () => {
    expect(component).toBeTruthy();
  });

  it('totals the original price of the selected products', () => {
    selectProducts('product-1', 'product-2');

    expect(component['selectedProductIds']()).toEqual(new Set(['product-1', 'product-2']));
    expect(component['model']().bundleOriginalPrice).toBe(30);
  });

  it('calculates the bundle price from a percentage discount', () => {
    selectProducts('product-1', 'product-2');

    component['handlePercentageBundlePrice'](numberEvent(10));

    expect(component['model']().bundlePrice).toBe(27);
  });

  it('uses the manual bundle price', () => {
    selectProducts('product-1', 'product-2');

    component['handleManualBundlePrice'](numberEvent(25));

    expect(component['model']().bundlePrice).toBe(25);
  });

  it('recalculates the price when removing products leaves a single item', () => {
    fillDetails();
    selectProducts('product-1', 'product-2');
    component['handleManualBundlePrice'](numberEvent(25));

    component['removeProduct']('product-2');
    component['submit'](new Event('submit'));

    expect(component['model']().bundleOriginalPrice).toBe(10);
    expect(component['model']().bundlePrice).toBeNaN();
    expect(createBundle).not.toHaveBeenCalled();
  });

  it('rejects a bundle price that is not below the original price', () => {
    fillDetails();
    selectProducts('product-1', 'product-2');
    component['model'].update((current) => ({ ...current, bundlePrice: 30 }));

    component['submit'](new Event('submit'));

    expect(createBundle).not.toHaveBeenCalled();
    expect(component['errorMessage']()).toBe('Bundle price must be less than the original price.');
  });

  it('does not submit without any products', () => {
    fillDetails();

    component['submit'](new Event('submit'));

    expect(createBundle).not.toHaveBeenCalled();
  });

  it('creates the bundle and closes with it', () => {
    fillDetails();
    selectProducts('product-1', 'product-2');
    component['handleManualBundlePrice'](numberEvent(25));

    component['submit'](new Event('submit'));

    expect(createBundle).toHaveBeenCalledWith(
      expect.objectContaining({
        bundleName: 'Starter Kit',
        bundleDescription: 'Everything to get started',
        productIds: ['product-1', 'product-2'],
        bundlePrice: 25,
      }),
    );
    expect(dialogRef.close).toHaveBeenCalledWith(createdBundle);
  });

  it('shows the backend message when creation fails', () => {
    createBundle.mockReturnValue(throwError(() => ({ code: 400, message: 'Invalid bundle' })));
    fillDetails();
    selectProducts('product-1', 'product-2');
    component['handleManualBundlePrice'](numberEvent(25));

    component['submit'](new Event('submit'));
    fixture.detectChanges();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component['isSubmitting']()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Invalid bundle',
    );
  });
});
