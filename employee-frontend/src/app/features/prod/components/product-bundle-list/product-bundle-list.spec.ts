import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Bundle } from '@features/prod/models/bundle.model';
import { DeliveryType, Product, ProductStatus } from '@features/prod/models/product.model';
import { BundleService } from '@features/prod/services/bundle.service';
import { ImageService } from '@features/prod/services/image.service';
import { ProductService } from '@features/prod/services/product.service';
import { Roles } from '@shared/models/roles';

import { ProductBundleList } from './product-bundle-list';

describe('ProductBundleList', () => {
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
    productIds: ['product-1', 'product-2', 'missing-product'],
    originalTotalPrice: 30,
    bundlePrice: 25,
  };

  let getBundles: ReturnType<typeof vi.fn>;
  let deleteBundle: ReturnType<typeof vi.fn>;
  let getProducts: ReturnType<typeof vi.fn>;
  let dialogOpen: ReturnType<typeof vi.fn>;

  const findButton = (root: HTMLElement, label: string): HTMLButtonElement | undefined =>
    Array.from(root.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === label,
    );

  beforeEach(async () => {
    getBundles = vi.fn().mockReturnValue(of([bundle]));
    deleteBundle = vi.fn().mockReturnValue(of(undefined));
    getProducts = vi.fn().mockReturnValue(of(products));
    dialogOpen = vi.fn().mockReturnValue({ afterClosed: () => of(undefined) });
    TestBed.configureTestingModule({
      imports: [ProductBundleList],
      providers: [
        { provide: BundleService, useValue: { getBundles, deleteBundle } },
        { provide: ProductService, useValue: { getProducts } },
        { provide: ImageService, useValue: { getImage: () => of() } },
        { provide: AuthState, useValue: { roles: signal([Roles.admin]) } },
      ],
    });
    TestBed.overrideProvider(MatDialog, { useValue: { open: dialogOpen } });
    await TestBed.compileComponents();
  });

  it('lists bundles with their resolved products', () => {
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Starter Kit');
    expect(text).toContain('Dog food');
    expect(text).toContain('Horse Saddle');
    expect(fixture.nativeElement.querySelectorAll('.bundle-item').length).toBe(2);
  });

  it('shows an error and retries when loading fails', () => {
    getBundles.mockReturnValueOnce(throwError(() => new Error('boom')));
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Impossible de charger les ensembles.',
    );

    findButton(fixture.nativeElement, 'Try again')?.click();
    fixture.detectChanges();

    expect(getBundles).toHaveBeenCalledTimes(2);
  });

  it('shows the management buttons for a manageable user', () => {
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();

    expect(findButton(fixture.nativeElement, 'Ajouter un ensemble')).toBeTruthy();
    expect(findButton(fixture.nativeElement, 'Update')).toBeTruthy();
    expect(findButton(fixture.nativeElement, 'Delete')).toBeTruthy();
  });

  it('hides the management buttons for a user without product-management roles', () => {
    TestBed.overrideProvider(AuthState, { useValue: { roles: signal([Roles.vet]) } });
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();

    expect(findButton(fixture.nativeElement, 'Ajouter un ensemble')).toBeUndefined();
    expect(findButton(fixture.nativeElement, 'Update')).toBeUndefined();
    expect(findButton(fixture.nativeElement, 'Delete')).toBeUndefined();
  });

  it('deletes a bundle and reloads the list after confirmation', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(true) });
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();
    getBundles.mockClear();

    fixture.componentInstance['openBundleDelete'](bundle);

    expect(deleteBundle).toHaveBeenCalledWith('bundle-1');
    expect(getBundles).toHaveBeenCalled();
  });

  it('does not delete when the confirmation is cancelled', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(false) });
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();

    fixture.componentInstance['openBundleDelete'](bundle);

    expect(deleteBundle).not.toHaveBeenCalled();
  });

  it('reloads the list after a bundle is added', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(bundle) });
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();
    getBundles.mockClear();

    fixture.componentInstance['openAddBundle']();

    expect(dialogOpen).toHaveBeenCalledWith(
      expect.anything(),
      expect.objectContaining({ data: { products } }),
    );
    expect(getBundles).toHaveBeenCalled();
  });

  it('opens the update dialog with the bundle and reloads after saving', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(bundle) });
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();
    getBundles.mockClear();

    fixture.componentInstance['openUpdateBundle'](bundle);

    expect(dialogOpen).toHaveBeenCalledWith(
      expect.anything(),
      expect.objectContaining({ data: { products, bundle } }),
    );
    expect(getBundles).toHaveBeenCalled();
  });

  it('does not reload when the add dialog is cancelled', () => {
    const fixture = TestBed.createComponent(ProductBundleList);
    fixture.detectChanges();
    getBundles.mockClear();

    fixture.componentInstance['openAddBundle']();

    expect(getBundles).not.toHaveBeenCalled();
  });
});
