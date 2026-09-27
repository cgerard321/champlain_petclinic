import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of } from 'rxjs';

import { ImageService } from '@features/prod/services/image.service';
import { ProductService } from '@features/prod/services/product.service';
import { ProductDetailsDialog } from './product-details-dialog';

describe('ProductDetailsDialog', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDetailsDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { productId: 'product-1' } },
        {
          provide: ProductService,
          useValue: { getProduct: () => of({ productId: 'product-1' }) },
        },
        { provide: ImageService, useValue: { getImage: () => of(null) } },
      ],
    }).compileComponents();
  });

  it('creates the Details dialog', () => {
    const fixture = TestBed.createComponent(ProductDetailsDialog);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
