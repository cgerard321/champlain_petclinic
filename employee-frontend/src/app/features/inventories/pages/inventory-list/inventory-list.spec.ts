import { ComponentFixture, TestBed } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Subject, of, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';

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
    important: false,
  };

  const inventoryTwo: Inventory = {
    inventoryId: 'inv-2',
    inventoryName: 'Surgical',
    inventoryType: 'Equipment',
    inventoryDescription: 'Surgical tools',
    important: false,
  };

  const getInventories = vi.fn();
  const getInventoryTypes = vi.fn().mockReturnValue(EMPTY);
  const getQuantity = vi.fn();
  const createInventory = vi.fn();
  const updateInventory = vi.fn();
  const deleteInventory = vi.fn();
  const updateImportantStatus = vi.fn();

  const inventoryService = {
    getInventories,
    getInventoryTypes,
    getQuantity,
    createInventory,
    updateInventory,
    deleteInventory,
    updateImportantStatus,
  };

  let activeRoles: string[] = [Roles.admin];

  function setup(roles: string[] = [Roles.admin]): Subject<Inventory> {
    vi.clearAllMocks();
    activeRoles = roles;

    const stream = new Subject<Inventory>();
    getInventories.mockReturnValue(stream);
    getInventoryTypes.mockReturnValue(EMPTY);
    getQuantity.mockReturnValue(of(10));
    createInventory.mockReturnValue(of(inventoryOne));
    updateInventory.mockReturnValue(of(inventoryOne));
    deleteInventory.mockReturnValue(of(undefined));
    updateImportantStatus.mockReturnValue(of(undefined));

    TestBed.configureTestingModule({
      imports: [InventoryList],
      providers: [
        { provide: InventoryService, useValue: inventoryService },
        {
          provide: AuthState,
          useValue: {
            roles: () => activeRoles,
            hasRole: (r: string) => activeRoles.includes(r),
          },
        },
        { provide: ActivatedRoute, useValue: {} },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryList);
    component = fixture.componentInstance;
    fixture.detectChanges();

    return stream;
  }

  describe('full-access role (ADMIN / INVENTORY_MANAGER)', () => {
    it('exposes create/update/delete capabilities', () => {
      setup([Roles.admin]);

      expect(component['can']().canCreateInventory).toBe(true);
      expect(component['can']().canUpdateInventory).toBe(true);
      expect(component['can']().canDeleteInventory).toBe(true);
    });

    it('creates a new inventory and reflects it immediately', () => {
      setup([Roles.admin]);

      component['newInventory'].set({
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      });

      component['saveInventory'](new Event('submit', { cancelable: true }));

      expect(createInventory).toHaveBeenCalledWith({
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      });
      expect(component['inventories']()).toEqual([inventoryOne]);
      expect(component['showAddForm']()).toBe(false);
    });

    it('updates the inventory being edited instead of creating a new one', () => {
      const stream = setup([Roles.admin]);
      stream.next(inventoryOne);

      component['editInventory'](inventoryOne);
      expect(component['editingInventoryId']()).toBe('inv-1');
      expect(component['showAddForm']()).toBe(true);

      const updated = { ...inventoryOne, inventoryName: 'Pharmacy (updated)' };
      updateInventory.mockReturnValue(of(updated));

      component['saveInventory'](new Event('submit', { cancelable: true }));

      expect(updateInventory).toHaveBeenCalledWith('inv-1', {
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      });
      expect(createInventory).not.toHaveBeenCalled();
      expect(component['inventories']()).toEqual([updated]);
      expect(component['editingInventoryId']()).toBe(null);
    });

    it('shows a form error when saving fails', () => {
      setup([Roles.admin]);
      createInventory.mockReturnValue(throwError(() => new Error('failed')));

      component['newInventory'].set({
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      });
      component['saveInventory'](new Event('submit', { cancelable: true }));

      expect(component['formError']()).toBe('Unable to create inventory.');
    });

    it('deletes an inventory after confirmation and removes it locally', () => {
      const stream = setup([Roles.admin]);
      stream.next(inventoryOne);
      stream.next(inventoryTwo);

      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      component['deleteInventoryAction'](inventoryOne);

      expect(deleteInventory).toHaveBeenCalledWith('inv-1');
      expect(component['inventories']()).toEqual([inventoryTwo]);

      confirmSpy.mockRestore();
    });

    it('does not delete when the user cancels the confirmation', () => {
      const stream = setup([Roles.admin]);
      stream.next(inventoryOne);

      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);
      component['deleteInventoryAction'](inventoryOne);

      expect(deleteInventory).not.toHaveBeenCalled();
      expect(component['inventories']()).toEqual([inventoryOne]);

      confirmSpy.mockRestore();
    });
  });

  describe('VET', () => {
    it('has no create/update/delete capability', () => {
      setup([Roles.vet]);

      expect(component['can']().canCreateInventory).toBe(false);
      expect(component['can']().canUpdateInventory).toBe(false);
      expect(component['can']().canDeleteInventory).toBe(false);
    });

    it('editInventory and deleteInventoryAction are no-ops without the capability', () => {
      const stream = setup([Roles.vet]);
      stream.next(inventoryOne);

      component['editInventory'](inventoryOne);
      expect(component['showAddForm']()).toBe(false);

      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      component['deleteInventoryAction'](inventoryOne);
      expect(deleteInventory).not.toHaveBeenCalled();
      confirmSpy.mockRestore();
    });
  });

  describe('RECEPTIONIST', () => {
    it('has no create/update/delete capability and the SSE stream never opens', () => {
      setup([Roles.receptionist]);

      expect(getInventories).not.toHaveBeenCalled();
      expect(component['can']().canCreateInventory).toBe(false);
      expect(component['can']().canUpdateInventory).toBe(false);
      expect(component['can']().canDeleteInventory).toBe(false);
    });
  });
});