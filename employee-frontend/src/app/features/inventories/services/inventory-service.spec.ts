import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { InventoryService } from './inventory-service';

const API_BASE_URL = '/api/gateway/inventories';

describe('InventoryService', () => {
  let service: InventoryService;
  let http: HttpTestingController;

  const inventory = {
    inventoryId: '1',
    inventoryName: 'Main',
    inventoryType: 'Pharmacy',
    inventoryDescription: 'Main inventory',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InventoryService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(InventoryService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should fetch inventories from the API', () => {
    // Assert
    service.getInventories().subscribe((result) => {
      expect(result).toEqual(mockInventory);
    });

    // Match request starting with base URL to handle default pagination params
    const request = http.expectOne((req) => req.url.startsWith(API_BASE_URL));

    expect(request.request.method).toBe('GET');

    request.flush(mockInventory);
  });

  it('should omit importantOnly when the favorites filter is off', () => {
    // Act
    service.getInventories(false).subscribe();

    // Assert
    const request = http.expectOne('/api/gateway/inventories');

    expect(request.request.params.has('importantOnly')).toBe(false);

    request.flush('');
  });

  it('should request only favorites when the filter is on', () => {
    // Act
    service.getInventories(true).subscribe();

    // Assert
    const request = http.expectOne('/api/gateway/inventories?importantOnly=true');

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('importantOnly')).toBe('true');

    request.flush('');
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

  it('should patch the important status of an inventory', () => {
    // Act
    service.updateImportantStatus('1', true).subscribe();

    // Assert
    const request = http.expectOne('/api/gateway/inventories/1/important');

    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ important: true });

    request.flush(null);
  });
});
