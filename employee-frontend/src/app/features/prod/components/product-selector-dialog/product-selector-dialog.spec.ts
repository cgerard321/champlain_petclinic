import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { vi } from 'vitest';

import { DeliveryType, Product, ProductStatus } from '@features/prod/models/product.model';

import { ProductSelectorDialog, ProductSelectorDialogData } from './product-selector-dialog';

describe('ProductSelectorDialog', () => {
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
    { ...baseProduct, productId: 'product-2', productName: 'Horse Saddle' },
    { ...baseProduct, productId: 'product-3', productName: 'Cat Toy' },
  ];
  let dialogRef: { close: ReturnType<typeof vi.fn>; backdropClick: () => Subject<MouseEvent> };
  let backdropClick: Subject<MouseEvent>;

  const createComponent = (alreadySelected = new Set<string>()): ProductSelectorDialog => {
    TestBed.overrideProvider(MAT_DIALOG_DATA, {
      useValue: { alreadySelected, products } satisfies ProductSelectorDialogData,
    });
    const fixture = TestBed.createComponent(ProductSelectorDialog);
    fixture.detectChanges();
    return fixture.componentInstance;
  };

  beforeEach(async () => {
    backdropClick = new Subject<MouseEvent>();
    dialogRef = { close: vi.fn(), backdropClick: () => backdropClick };
    await TestBed.configureTestingModule({
      imports: [ProductSelectorDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { alreadySelected: new Set(), products } },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();
  });

  it('starts with the already selected products', () => {
    const component = createComponent(new Set(['product-2']));

    expect(component['isSelected']('product-2')).toBe(true);
    expect(component['isSelected']('product-1')).toBe(false);
  });

  it('toggles products and closes with the selection', () => {
    const component = createComponent();

    component['toggleProduct']('product-1', true);
    component['toggleProduct']('product-3', true);
    component['toggleProduct']('product-1', false);
    component['close']();

    expect(dialogRef.close).toHaveBeenCalledWith(new Set(['product-3']));
  });

  it('filters products by name ignoring case and surrounding spaces', () => {
    const component = createComponent();

    component['searchProducts']('  SADDLE ');

    expect(component['filteredProducts']().map((product) => product.productId)).toEqual([
      'product-2',
    ]);
  });

  it('adds all products and clears the selection', () => {
    const component = createComponent();

    component['addAll']();
    expect(component['selectedProductIds']().size).toBe(3);

    component['clear']();
    expect(component['selectedProductIds']().size).toBe(0);
  });

  it('closes without a result when cancelled', () => {
    const component = createComponent(new Set(['product-1']));

    component['cancel']();

    expect(dialogRef.close).toHaveBeenCalledWith();
  });

  it('closes with the selection when the backdrop is clicked', () => {
    createComponent(new Set(['product-1']));

    backdropClick.next(new MouseEvent('click'));

    expect(dialogRef.close).toHaveBeenCalledWith(new Set(['product-1']));
  });
});
