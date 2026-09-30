import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import {
  DeliveryType,
  Product,
  ProductRequest,
  ProductStatus,
} from '@features/prod/models/product.model';

import { ProductService } from './product.service';

describe('ProductService', () => {
  let service: ProductService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('parses data records from the Product SSE response', () => {
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
    let result: Product[] | undefined;

    service.getProducts().subscribe((products) => (result = products));

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/gateway/products' &&
        candidate.params.get('includeImage') === 'true',
    );
    expect(request.request.responseType).toBe('text');
    request.flush(
      `data:${JSON.stringify(product)}\n\ndata:${JSON.stringify({ ...product, productId: 'product-2' })}\n\n`,
    );

    expect(result?.map((item) => item.productId)).toEqual(['product-1', 'product-2']);
  });

  it('sends a product name search parameter', () => {
    service.getProducts({ productName: 'horse saddle' }).subscribe();

    const request = http.expectOne('/api/gateway/products?productName=horse%20saddle');
    expect(request.request.method).toBe('GET');
    request.flush('');
  });

  it('creates a product with JSON', () => {
    const requestBody: ProductRequest = {
      productName: 'Dog food',
      productDescription: 'Food',
      productSalePrice: 10,
      productQuantity: 5,
      isUnlisted: false,
      productTypeId: '586d0700-57db-4312-b6f1-413b79dd018c',
      deliveryType: DeliveryType.DELIVERY,
    };

    service.createProduct(requestBody).subscribe();

    const request = http.expectOne('/api/gateway/products');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(requestBody);
    request.flush({
      ...requestBody,
      productId: 'product-1',
      productStatus: ProductStatus.AVAILABLE,
    });
  });

  it('loads product enums', () => {
    service.getProductEnums().subscribe();

    const request = http.expectOne('/api/gateway/products/enums');
    expect(request.request.method).toBe('GET');
    request.flush({ productType: [], productStatus: [], deliveryType: [] });
  });

  it('updates a product with JSON', () => {
    const requestBody: ProductRequest = {
      productName: 'Updated food',
      productDescription: 'Updated description',
      productSalePrice: 12,
      productQuantity: 8,
      isUnlisted: false,
      productTypeId: '586d0700-57db-4312-b6f1-413b79dd018c',
      deliveryType: DeliveryType.DELIVERY,
    };

    service.updateProduct('product-1', requestBody).subscribe();

    const request = http.expectOne('/api/gateway/products/product-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(requestBody);
    request.flush({
      ...requestBody,
      productId: 'product-1',
      productStatus: ProductStatus.AVAILABLE,
    });
  });

  it('updates a product image through the image subresource', () => {
    const image = {
      fileName: 'product.png',
      fileType: 'image/png',
      fileData: 'aW1hZ2U=',
    };

    service.updateProductImage('product-1', image).subscribe();

    const request = http.expectOne('/api/gateway/products/product-1/image');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(image);
    request.flush({ productId: 'product-1', image });
  });
});
