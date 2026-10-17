import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductBundleList } from './product-bundle-list';

describe('ProductBundleList', () => {
  let component: ProductBundleList;
  let fixture: ComponentFixture<ProductBundleList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductBundleList],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductBundleList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
