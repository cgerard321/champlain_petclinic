import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductSelectorDialog } from './product-selector-dialog';

describe('ProductSelectorDialog', () => {
  let component: ProductSelectorDialog;
  let fixture: ComponentFixture<ProductSelectorDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductSelectorDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductSelectorDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
