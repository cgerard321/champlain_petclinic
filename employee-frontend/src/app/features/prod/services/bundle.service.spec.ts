import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Bundle, BundleRequest } from '@features/prod/models/bundle.model';

import { BundleService } from './bundle.service';

describe('BundleService', () => {
  let service: BundleService;
  let http: HttpTestingController;
  const bundle: Bundle = {
    bundleId: 'bundle-1',
    bundleName: 'Starter Kit',
    bundleDescription: 'Everything to get started',
    productIds: ['product-1', 'product-2'],
    originalTotalPrice: 30,
    bundlePrice: 25,
  };
  const request: BundleRequest = {
    bundleName: 'Starter Kit',
    bundleDescription: 'Everything to get started',
    productIds: ['product-1', 'product-2'],
    bundlePrice: 25,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BundleService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BundleService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads all bundles', () => {
    let result: Bundle[] | undefined;

    service.getBundles().subscribe((bundles) => (result = bundles));

    const call = http.expectOne('/api/gateway/products/bundles');
    expect(call.request.method).toBe('GET');
    call.flush([bundle]);

    expect(result).toEqual([bundle]);
  });

  it('creates a bundle', () => {
    let result: Bundle | undefined;

    service.createBundle(request).subscribe((created) => (result = created));

    const call = http.expectOne('/api/gateway/products/bundles');
    expect(call.request.method).toBe('POST');
    expect(call.request.body).toEqual(request);
    call.flush(bundle);

    expect(result).toEqual(bundle);
  });

  it('updates a bundle', () => {
    let result: Bundle | undefined;

    service.updateBundle('bundle-1', request).subscribe((updated) => (result = updated));

    const call = http.expectOne('/api/gateway/products/bundles/bundle-1');
    expect(call.request.method).toBe('PUT');
    expect(call.request.body).toEqual(request);
    call.flush(bundle);

    expect(result).toEqual(bundle);
  });

  it('deletes a bundle', () => {
    service.deleteBundle('bundle-1').subscribe();

    const call = http.expectOne('/api/gateway/products/bundles/bundle-1');
    expect(call.request.method).toBe('DELETE');
    call.flush(null);
  });
});
