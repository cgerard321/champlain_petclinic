import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { vi } from 'vitest';

import { ProductDeleteDialog, ProductDeleteDialogData } from './product-delete-dialog';

describe('ProductDeleteDialog', () => {
  const data: ProductDeleteDialogData = {
    title: 'Delete product?',
    message: 'Are you sure?',
    confirmLabel: 'Delete',
  };
  const dialogRef = { close: vi.fn() };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductDeleteDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();
  });

  it('renders the supplied confirmation content', () => {
    const fixture = TestBed.createComponent(ProductDeleteDialog);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Delete product?');
    expect(fixture.nativeElement.textContent).toContain('Are you sure?');
    expect(fixture.nativeElement.textContent).toContain('Delete');
  });

  it('closes with true when confirmed', () => {
    const fixture = TestBed.createComponent(ProductDeleteDialog);

    fixture.componentInstance['confirm']();

    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });

  it('closes with false when cancelled', () => {
    const fixture = TestBed.createComponent(ProductDeleteDialog);

    fixture.componentInstance['cancel']();

    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });
});
