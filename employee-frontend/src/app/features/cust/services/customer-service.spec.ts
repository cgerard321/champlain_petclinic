import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CustomerService } from './customer-service';

describe('CustomerService', () => {
  let service: CustomerService;
  let http: HttpTestingController;

  const customerId = 'e6c7398e-8ac4-4e10-9ee0-03ef33f0361a';
  const url = `/api/gateway/customers/${customerId}/soft`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CustomerService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CustomerService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  describe('softDeleteCustomer', () => {
    it('should send a DELETE to the soft delete endpoint', () => {
      let completed = false;
      service.softDeleteCustomer(customerId).subscribe({ complete: () => (completed = true) });

      const request = http.expectOne(url);
      expect(request.request.method).toBe('DELETE');
      request.flush(null, { status: 204, statusText: 'No Content' });

      expect(completed).toBe(true);
    });

    it('should return the error when the deactivation fails', () => {
      let status = 0;
      service.softDeleteCustomer(customerId).subscribe({
        error: (error: HttpErrorResponse) => (status = error.status),
      });

      http.expectOne(url).flush('Not found', { status: 404, statusText: 'Not Found' });

      expect(status).toBe(404);
    });
  });
});
