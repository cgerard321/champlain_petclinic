import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { InventoryService } from './inventory-service';

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
    // Arrange
    const inventory = {
      inventoryId: '1',
      inventoryName: 'Main',
      inventoryType: 'Pharmacy',
      inventoryDescription: 'Main inventory',
    };

    // Assert
    service.getInventories().subscribe((result) => {
      expect(result).toEqual([inventory]);
    });

    const request = http.expectOne('/api/gateway/inventories');

    expect(request.request.method).toBe('GET');

    request.flush(`data: ${JSON.stringify(inventory)}`);
  });

  it('should fetch the product quantity for an inventory', () => {
    // Assert
    service.getQuantity('1').subscribe((result) => {
      expect(result).toBe(25);
    });

    const request = http.expectOne('/api/gateway/inventories/1/productquantity');

    expect(request.request.method).toBe('GET');

    request.flush(25);
  });
});
