import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductBundleUpdateDialog } from './product-bundle-update-dialog';

describe('ProductBundleUpdateDialog', () => {
  let component: ProductBundleUpdateDialog;
  let fixture: ComponentFixture<ProductBundleUpdateDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductBundleUpdateDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductBundleUpdateDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
