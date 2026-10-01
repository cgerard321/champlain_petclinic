import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { InventoryService } from './inventory-service';

const API_BASE_URL = '/api/gateway/inventories';

describe('InventoryService', () => {
  let service: InventoryService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InventoryService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(InventoryService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should fetch inventories from the API', () => {
    const mockInventory = {
      inventoryId: '1',
      inventoryName: 'Main',
      inventoryType: 'Pharmacy',
      inventoryDescription: 'Main inventory',
    };

    service.getInventories().subscribe((result) => {
      expect(result).toEqual(mockInventory);
    });

    // Match request starting with base URL to handle default pagination params
    const request = http.expectOne((req) => req.url.startsWith(API_BASE_URL));

    expect(request.request.method).toBe('GET');

    request.flush(mockInventory);
  });

  it('should fetch the product quantity for an inventory', () => {
    const mockInventoryId = '1';
    const mockQuantity = 25;

    service.getQuantity(mockInventoryId).subscribe((result) => {
      expect(result).toBe(mockQuantity);
    });

    const request = http.expectOne(`${API_BASE_URL}/${mockInventoryId}/productquantity`);

    expect(request.request.method).toBe('GET');

    request.flush(mockQuantity);
  });
});
