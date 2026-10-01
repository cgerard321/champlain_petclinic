import { TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { ImageService } from '@features/prod/services/image.service';
import { ProductService } from '@features/prod/services/product.service';

import { ProductAddDialog } from './product-add-dialog';

describe('ProductAddDialog', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductAddDialog],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => undefined } },
        {
          provide: ProductService,
          useValue: { createProduct: () => of({ productId: 'product-1' }) },
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

  it('creates the Add Product dialog', () => {
    const fixture = TestBed.createComponent(ProductAddDialog);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
