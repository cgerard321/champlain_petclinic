import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Bundle } from '@features/prod/models/bundle.model';
import { DeliveryType, Product, ProductStatus } from '@features/prod/models/product.model';
import { BundleService } from '@features/prod/services/bundle.service';

import { ProductBundleUpdateDialog } from './product-bundle-update-dialog';

describe('ProductBundleUpdateDialog', () => {
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
  const bundle: Bundle = {
    bundleId: 'bundle-1',
    bundleName: 'Starter Kit',
    bundleDescription: 'Everything to get started',
    productIds: ['product-1', 'product-2'],
    originalTotalPrice: 30,
    bundlePrice: 25,
  };
  const numberEvent = (value: number): Event =>
    ({ target: { valueAsNumber: value } }) as unknown as Event;

  let fixture: ComponentFixture<ProductBundleUpdateDialog>;
  let component: ProductBundleUpdateDialog;
  let dialogRef: { close: ReturnType<typeof vi.fn> };
  let dialogOpen: ReturnType<typeof vi.fn>;
  let updateBundle: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };
    dialogOpen = vi.fn();
    updateBundle = vi.fn().mockReturnValue(of(bundle));
    TestBed.configureTestingModule({
      imports: [ProductBundleUpdateDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { products, bundle } },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: BundleService, useValue: { updateBundle } },
      ],
    });
    TestBed.overrideProvider(MatDialog, { useValue: { open: dialogOpen } });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(ProductBundleUpdateDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('prefills the form from the bundle', () => {
    expect(component['model']()).toEqual({
      bundleName: 'Starter Kit',
      bundleDescription: 'Everything to get started',
      bundlePrice: 25,
      bundleOriginalPrice: 30,
      productIds: new Set(['product-1', 'product-2']),
    });
    expect(component['selectedProductIds']()).toEqual(new Set(['product-1', 'product-2']));
    expect(component['selectedTabIndex']()).toBe(1);
    expect(component['discountManual']()).toBe(25);
  });

  it('updates the bundle and closes with the result', () => {
    component['submit'](new Event('submit'));

    expect(updateBundle).toHaveBeenCalledWith(
      'bundle-1',
      expect.objectContaining({
        bundleName: 'Starter Kit',
        bundleDescription: 'Everything to get started',
        productIds: ['product-1', 'product-2'],
        bundlePrice: 25,
      }),
    );
    expect(dialogRef.close).toHaveBeenCalledWith(bundle);
  });

  it('recalculates the bundle price from a percentage discount', () => {
    component['handlePercentageBundlePrice'](numberEvent(50));

    expect(component['model']().bundlePrice).toBe(15);
  });

  it('adds products picked in the selector', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(new Set(['product-2'])) });

    component['openProductSelector']();

    expect(component['selectedProductIds']()).toEqual(new Set(['product-2']));
    expect(component['model']().bundleOriginalPrice).toBe(20);
  });

  it('blocks the update when removing products leaves the price too high', () => {
    component['removeProduct']('product-2');
    component['submit'](new Event('submit'));

    expect(component['model']().bundleOriginalPrice).toBe(10);
    expect(component['model']().bundlePrice).toBeNaN();
    expect(updateBundle).not.toHaveBeenCalled();
  });

  it('rejects a bundle price that is not below the original price', () => {
    component['model'].update((current) => ({ ...current, bundlePrice: 30 }));

    component['submit'](new Event('submit'));

    expect(updateBundle).not.toHaveBeenCalled();
    expect(component['errorMessage']()).toBe('Bundle price must be less than the original price.');
  });

  it('shows the backend message when the update fails', () => {
    updateBundle.mockReturnValue(throwError(() => ({ code: 404, message: 'Bundle not found' })));

    component['submit'](new Event('submit'));
    fixture.detectChanges();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component['isSubmitting']()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Bundle not found',
    );
  });
});
