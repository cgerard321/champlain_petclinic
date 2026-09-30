import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';
import { describe, it, expect, vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Inventory } from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';
import { Roles } from '@shared/models/roles';

import { InventoryList } from './inventory-list';

describe('InventoryList', () => {
  let fixture: ComponentFixture<InventoryList>;
  let component: InventoryList;

  const inventoryOne: Inventory = {
    inventoryId: 'inv-1',
    inventoryName: 'Pharmacy',
    inventoryType: 'Medication',
    inventoryDescription: 'Prescription drugs',
  };

  const inventoryTwo: Inventory = {
    inventoryId: 'inv-2',
    inventoryName: 'Surgical',
    inventoryType: 'Equipment',
    inventoryDescription: 'Surgical tools',
  };

  const getInventories = vi.fn<InventoryService['getInventories']>();
  const getQuantity = vi.fn<InventoryService['getQuantity']>();

  const inventoryService = { getInventories, getQuantity };

  // Streams items one at a time via .next(), matching the real SSE-based
  // service — getInventories() emits ONE Inventory per event, not an array.
  function setup(roles: Roles[]) {
    vi.clearAllMocks();

    const stream = new Subject<Inventory>();
    getInventories.mockReturnValue(stream);
    getQuantity.mockReturnValue(of(10));

    TestBed.configureTestingModule({
      imports: [InventoryList],
      providers: [
        { provide: InventoryService, useValue: inventoryService },
        { provide: AuthState, useValue: { roles: () => roles } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryList);
    component = fixture.componentInstance;
    fixture.detectChanges();

    return stream;
  }

  describe('full-access role (ADMIN / INVENTORY_MANAGER)', () => {
    it('appends items as they stream in and loads quantities for each', () => {
      const stream = setup([Roles.admin]);

      stream.next(inventoryOne);
      expect(component['inventories']()).toEqual([inventoryOne]);
      expect(getQuantity).toHaveBeenCalledWith('inv-1');

      stream.next(inventoryTwo);
      expect(component['inventories']()).toEqual([inventoryOne, inventoryTwo]);
      expect(getQuantity).toHaveBeenCalledWith('inv-2');

      expect(component['isLoading']()).toBe(false);
    });

    it('replaces an existing item rather than duplicating it', () => {
      const stream = setup([Roles.admin]);

      stream.next(inventoryOne);
      stream.next({ ...inventoryOne, inventoryName: 'Pharmacy (updated)' });

      expect(component['inventories']()).toEqual([
        { ...inventoryOne, inventoryName: 'Pharmacy (updated)' },
      ]);
    });

    it('sets an ApiError on stream error', () => {
      const stream = setup([Roles.admin]);

      stream.error({ code: 'NETWORK', message: 'Connection lost' });

      expect(component['errorMessage']()).toEqual({
        code: 'NETWORK',
        message: 'Connection lost',
      });
      expect(component['isLoading']()).toBe(false);
    });
  });

  describe('VET', () => {
    it('receives streamed items (list-level SSE is VET-allowed) but never calls getQuantity', () => {
      const stream = setup([Roles.vet]);

      stream.next(inventoryOne);

      expect(component['inventories']()).toEqual([inventoryOne]);
      expect(getQuantity).not.toHaveBeenCalled();
      expect(component['quantityFor']('inv-1')).toBe(null);
    });
  });

  describe('RECEPTIONIST', () => {
    it('never opens the SSE connection and clears loading immediately', () => {
      setup([Roles.receptionist]);

      expect(getInventories).not.toHaveBeenCalled();
      expect(component['isLoading']()).toBe(false);
      expect(component['can']().hasAnyAccess).toBe(false);
    });
  });
});
