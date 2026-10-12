import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { DeliveryType, Product, ProductStatus } from '@features/prod/models/product.model';
import { ProductService } from '@features/prod/services/product.service';
import { Roles } from '@shared/models/roles';

import { Prod } from './prod';

describe('Prod', () => {
  let getProducts: ReturnType<typeof vi.fn>;
  let deleteProduct: ReturnType<typeof vi.fn>;
  let dialogOpen: ReturnType<typeof vi.fn>;
  const product: Product = {
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

  beforeEach(async () => {
    vi.useFakeTimers();
    getProducts = vi.fn().mockReturnValue(of([product]));
    deleteProduct = vi.fn().mockReturnValue(of(product));
    dialogOpen = vi.fn().mockReturnValue({ afterClosed: () => of(undefined) });
    TestBed.configureTestingModule({
      imports: [Prod],
      providers: [
        { provide: ProductService, useValue: { getProducts, deleteProduct } },
        { provide: AuthState, useValue: { roles: signal([Roles.admin]) } },
      ],
    });
    TestBed.overrideProvider(MatDialog, { useValue: { open: dialogOpen } });
    await TestBed.compileComponents();
  });

  afterEach(() => {
    vi.useRealTimers();
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

  it('searches by product name after the debounce period', () => {
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();
    getProducts.mockClear();

    const input = fixture.nativeElement.querySelector('input[type="search"]');
    input.value = 'horse';
    input.dispatchEvent(new Event('input'));

    vi.advanceTimersByTime(299);
    expect(getProducts).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(getProducts).toHaveBeenCalledWith({ productName: 'horse' });

    fixture.componentInstance['loadProducts']();
    expect(getProducts).toHaveBeenLastCalledWith({ productName: 'horse' });
  });

  it('reloads all products when the search is cleared', () => {
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector('input[type="search"]');
    input.value = 'horse';
    input.dispatchEvent(new Event('input'));
    vi.advanceTimersByTime(300);
    getProducts.mockClear();

    input.value = '';
    input.dispatchEvent(new Event('input'));
    vi.advanceTimersByTime(300);

    expect(getProducts).toHaveBeenCalledWith({});
  });

  it('keeps the search results when the initial load finishes after a search', () => {
    const initialLoad = new Subject<Product[]>();
    const horseSaddle: Product = {
      ...product,
      productId: 'product-2',
      productName: 'Horse Saddle',
    };
    getProducts
      .mockReset()
      .mockImplementationOnce(() => initialLoad)
      .mockReturnValue(of([horseSaddle]));

    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector('input[type="search"]');
    input.value = 'horse';
    input.dispatchEvent(new Event('input'));
    vi.advanceTimersByTime(300);
    fixture.detectChanges();

    initialLoad.next([product, horseSaddle]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Horse Saddle');
    expect(fixture.nativeElement.textContent).not.toContain('Dog food');
  });

  it('keeps the search term when products are reloaded', () => {
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector('input[type="search"]');
    input.value = 'horse';
    input.dispatchEvent(new Event('input'));
    vi.advanceTimersByTime(300);
    getProducts.mockClear();

    fixture.componentInstance['loadProducts']();

    expect(getProducts).toHaveBeenLastCalledWith({ productName: 'horse' });
  });

  it('shows the Delete button for a manageable user', () => {
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll(
      'button',
    ) as NodeListOf<HTMLButtonElement>;
    const deleteButton = Array.from(buttons).find(
      (button) => button.textContent.trim() === 'Delete',
    );

    expect(deleteButton).toBeTruthy();
  });

  it('hides the Delete button for a user without product-management roles', () => {
    TestBed.overrideProvider(AuthState, { useValue: { roles: signal([Roles.vet]) } });
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll(
      'button',
    ) as NodeListOf<HTMLButtonElement>;
    const deleteButton = Array.from(buttons).find(
      (button) => button.textContent.trim() === 'Delete',
    );

    expect(deleteButton).toBeUndefined();
  });

  it('deletes a product and reloads the list after confirmation', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(true) });
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();
    getProducts.mockClear();

    fixture.componentInstance['openProductDelete'](product);

    expect(deleteProduct).toHaveBeenCalledWith('product-1', false);
    expect(getProducts).toHaveBeenCalled();
  });

  it('does not delete when the confirmation is cancelled', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(false) });
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();
    deleteProduct.mockClear();

    fixture.componentInstance['openProductDelete'](product);

    expect(deleteProduct).not.toHaveBeenCalled();
  });

  it('asks about bundles and retries with cascading enabled after a conflict', () => {
    deleteProduct
      .mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 409 })))
      .mockReturnValueOnce(of(product));
    dialogOpen
      .mockReturnValueOnce({ afterClosed: () => of(true) })
      .mockReturnValueOnce({ afterClosed: () => of(true) });
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    fixture.componentInstance['openProductDelete'](product);

    expect(deleteProduct).toHaveBeenNthCalledWith(1, 'product-1', false);
    expect(deleteProduct).toHaveBeenNthCalledWith(2, 'product-1', true);
    expect(dialogOpen).toHaveBeenCalledTimes(2);
  });

  it('asks about bundles and retries with cascading when the API error code is 409', () => {
    deleteProduct
      .mockReturnValueOnce(
        throwError(() => ({ code: 409, message: 'Product is part of one or more bundles' })),
      )
      .mockReturnValueOnce(of(product));
    dialogOpen
      .mockReturnValueOnce({ afterClosed: () => of(true) })
      .mockReturnValueOnce({ afterClosed: () => of(true) });
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    fixture.componentInstance['openProductDelete'](product);

    expect(deleteProduct).toHaveBeenNthCalledWith(1, 'product-1', false);
    expect(deleteProduct).toHaveBeenNthCalledWith(2, 'product-1', true);
    expect(dialogOpen).toHaveBeenCalledTimes(2);
  });

  it('shows the backend message when deletion fails with a non-conflict error', () => {
    dialogOpen.mockReturnValue({ afterClosed: () => of(true) });
    deleteProduct.mockReturnValue(
      throwError(() => ({ code: 404, message: 'Product not found: product-1' })),
    );
    const fixture = TestBed.createComponent(Prod);
    fixture.detectChanges();

    fixture.componentInstance['openProductDelete'](product);
    fixture.detectChanges();

    expect(deleteProduct).toHaveBeenCalledOnce();
    expect(dialogOpen).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Product not found: product-1',
    );
  });
});
