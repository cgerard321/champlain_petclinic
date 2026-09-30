import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
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
  const createInventory = vi.fn<InventoryService['createInventory']>();
  const updateInventory = vi.fn<InventoryService['updateInventory']>();
  const deleteInventory = vi.fn<InventoryService['deleteInventory']>();

  const inventoryService = {
    getInventories,
    getQuantity,
    createInventory,
    updateInventory,
    deleteInventory,
  };

  function setup(roles: Roles[]) {
    vi.clearAllMocks();

    const stream = new Subject<Inventory>();
    getInventories.mockReturnValue(stream);
    getQuantity.mockReturnValue(of(10));
    createInventory.mockReturnValue(of(inventoryOne));
    updateInventory.mockReturnValue(of(inventoryOne));
    deleteInventory.mockReturnValue(of(void 0));

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
    it('exposes create/update/delete capabilities', () => {
      setup([Roles.admin]);

      expect(component['can']().canCreateInventory).toBe(true);
      expect(component['can']().canUpdateInventory).toBe(true);
      expect(component['can']().canDeleteInventory).toBe(true);
    });

    it('creates a new inventory and reflects it immediately, without waiting on the SSE stream', () => {
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
      // Reflected locally right away — not dependent on the SSE stream
      // ever emitting anything.
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