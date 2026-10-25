import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductBundleAddDialog } from './product-bundle-add-dialog';

describe('ProductBundleAddDialog', () => {
  let component: ProductBundleAddDialog;
  let fixture: ComponentFixture<ProductBundleAddDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductBundleAddDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductBundleAddDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
