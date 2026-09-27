import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Status } from '@features/supplies/models/status';
import { Supply } from '@features/supplies/models/supply';

import { SupplyService } from './supply-service';

describe('SupplyService', () => {
  let service: SupplyService;
  let http: HttpTestingController;

  const inventoryId = 'inventory-1';
  const productId = 'product-1';

  const supplyOne: Supply = {
    productId: 'product-1',
    inventoryId,
    productName: 'Bandages',
    productDescription: 'Medical bandages',
    productPrice: 10,
    productQuantity: 20,
    productSalePrice: 15,
    status: Status.AVAILABLE,
    lastUpdatedAt: '2026-09-26T12:00:00',
  };

  const supplyTwo: Supply = {
    productId: 'product-2',
    inventoryId,
    productName: 'Syringes',
    productDescription: 'Disposable syringes',
    productPrice: 5,
    productQuantity: 50,
    productSalePrice: 8,
    status: Status.RE_ORDER,
    lastUpdatedAt: '2026-09-26T13:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SupplyService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(SupplyService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  describe('getSupplies', () => {
    it('should fetch and parse multiple supplies from an SSE response', () => {
      let result: Supply[] | undefined;

      service.getSupplies(inventoryId).subscribe((supplies) => {
        result = supplies;
      });

      const request = http.expectOne(`/api/gateway/inventories/${inventoryId}/products`);

      expect(request.request.method).toBe('GET');
      expect(request.request.responseType).toBe('text');

      request.flush(
        `data:${JSON.stringify(supplyOne)}\n` + `\n` + `data:${JSON.stringify(supplyTwo)}\n`,
      );

      expect(result).toEqual([supplyOne, supplyTwo]);
    });

    it('should return an empty array when the inventory has no supplies', () => {
      let result: Supply[] | undefined;

      service.getSupplies(inventoryId).subscribe((supplies) => {
        result = supplies;
      });

      const request = http.expectOne(`/api/gateway/inventories/${inventoryId}/products`);

      request.flush('');

      expect(result).toEqual([]);
    });

    it('should ignore SSE lines that are not data events', () => {
      let result: Supply[] | undefined;

      service.getSupplies(inventoryId).subscribe((supplies) => {
        result = supplies;
      });

      const request = http.expectOne(`/api/gateway/inventories/${inventoryId}/products`);

      request.flush(`event:message\n` + `data:${JSON.stringify(supplyOne)}\n` + `retry:1000\n`);

      expect(result).toEqual([supplyOne]);
    });

    it('should propagate an HTTP error when loading supplies fails', () => {
      let receivedError: HttpErrorResponse | undefined;

      service.getSupplies(inventoryId).subscribe({
        error: (error: HttpErrorResponse) => {
          receivedError = error;
        },
      });

      const request = http.expectOne(`/api/gateway/inventories/${inventoryId}/products`);

      request.flush('Server error', {
        status: 500,
        statusText: 'Internal Server Error',
      });

      expect(receivedError?.status).toBe(500);
    });
  });

  describe('createSupply', () => {
    it('should send the supply data to the correct inventory endpoint', () => {
      const requestBody = {
        productName: 'Bandages',
        productDescription: 'Medical bandages',
        productPrice: 10,
        productQuantity: 20,
        productSalePrice: 15,
      };

      let result: Supply | undefined;

      service.createSupply(inventoryId, requestBody).subscribe((supply) => {
        result = supply;
      });

      const request = http.expectOne(`/api/gateway/inventories/${inventoryId}/products`);

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(requestBody);

      request.flush(supplyOne);

      expect(result).toEqual(supplyOne);
    });

    it('should propagate an HTTP error when creating a supply fails', () => {
      const requestBody = {
        productName: 'Bandages',
        productDescription: 'Medical bandages',
        productPrice: 10,
        productQuantity: 20,
        productSalePrice: 15,
      };

      let receivedError: HttpErrorResponse | undefined;

      service.createSupply(inventoryId, requestBody).subscribe({
        error: (error: HttpErrorResponse) => {
          receivedError = error;
        },
      });

      const request = http.expectOne(`/api/gateway/inventories/${inventoryId}/products`);

      request.flush('Invalid supply', {
        status: 400,
        statusText: 'Bad Request',
      });

      expect(receivedError?.status).toBe(400);
    });
  });

  describe('updateSupply', () => {
    it('should update the correct supply with the supplied values', () => {
      const requestBody = {
        productName: 'Updated Bandages',
        productDescription: 'Updated medical bandages',
        productPrice: 12,
        productQuantity: 30,
        productSalePrice: 18,
      };

      const updatedSupply: Supply = {
        ...supplyOne,
        ...requestBody,
      };

      let result: Supply | undefined;

      service.updateSupply(inventoryId, productId, requestBody).subscribe((supply) => {
        result = supply;
      });

      const request = http.expectOne(
        `/api/gateway/inventories/${inventoryId}/products/${productId}`,
      );

      expect(request.request.method).toBe('PUT');
      expect(request.request.body).toEqual(requestBody);

      request.flush(updatedSupply);

      expect(result).toEqual(updatedSupply);
    });

    it('should propagate an HTTP error when updating a supply fails', () => {
      const requestBody = {
        productName: 'Updated Bandages',
        productDescription: 'Updated medical bandages',
        productPrice: 12,
        productQuantity: 30,
        productSalePrice: 18,
      };

      let receivedError: HttpErrorResponse | undefined;

      service.updateSupply(inventoryId, productId, requestBody).subscribe({
        error: (error: HttpErrorResponse) => {
          receivedError = error;
        },
      });

      const request = http.expectOne(
        `/api/gateway/inventories/${inventoryId}/products/${productId}`,
      );

      request.flush('Supply not found', {
        status: 404,
        statusText: 'Not Found',
      });

      expect(receivedError?.status).toBe(404);
    });
  });

  describe('deleteSupply', () => {
    it('should delete the correct supply', () => {
      let completed = false;

      service.deleteSupply(inventoryId, productId).subscribe({
        complete: () => {
          completed = true;
        },
      });

      const request = http.expectOne(
        `/api/gateway/inventories/${inventoryId}/products/${productId}`,
      );

      expect(request.request.method).toBe('DELETE');

      request.flush(null);

      expect(completed).toBe(true);
    });

    it('should propagate an HTTP error when deleting a supply fails', () => {
      let receivedError: HttpErrorResponse | undefined;

      service.deleteSupply(inventoryId, productId).subscribe({
        error: (error: HttpErrorResponse) => {
          receivedError = error;
        },
      });

      const request = http.expectOne(
        `/api/gateway/inventories/${inventoryId}/products/${productId}`,
      );

      request.flush('Supply not found', {
        status: 404,
        statusText: 'Not Found',
      });

      expect(receivedError?.status).toBe(404);
    });
  });
});
