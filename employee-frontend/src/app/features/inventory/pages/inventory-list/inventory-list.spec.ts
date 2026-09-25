import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { InventoryList } from './inventory-list';
import { InventoryService } from '@features/inventory/services/inventory-service';

describe('InventoryList', () => {
  let fixture: ComponentFixture<InventoryList>;

  const inventoryService = {
    getInventories: vi.fn(),
    getQuantity: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    inventoryService.getInventories.mockReturnValue(of([]));
    inventoryService.getQuantity.mockReturnValue(of(10));

    await TestBed.configureTestingModule({
      imports: [InventoryList],
      providers: [
        {
          provide: InventoryService,
          useValue: inventoryService,
        },
        {
          provide: ActivatedRoute,
          useValue: {},
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryList);
  });

  it('should create', () => {
    // Assert
    expect(fixture.componentInstance).toBeTruthy();
  });
});
