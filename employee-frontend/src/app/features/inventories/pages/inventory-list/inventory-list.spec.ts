import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Subject, of, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Inventory } from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import { ModalFormData, ModalFormResult } from '@shared/components/modal-form/modal-form-models';
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

  let dialogClosed: Subject<ModalFormResult<Record<string, unknown>> | undefined>;
  const openDialog = vi.fn(
    (dialogComponent: unknown, config?: { data?: ModalFormData<Record<string, unknown>> }) => {
      expect(dialogComponent).toBeDefined();
      expect(config).toBeDefined();
      return {
        afterClosed: () => dialogClosed.asObservable(),
      };
    },
  );

  let activeRoles: string[] = [Roles.admin];

  function setup(roles: string[] = [Roles.admin]): Subject<Inventory> {
    vi.clearAllMocks();
    activeRoles = roles;
    dialogClosed = new Subject();

    const stream = new Subject<Inventory>();
    getInventories.mockReturnValue(stream);
    getInventoryTypes.mockReturnValue(of({ typeId: 'type-1', type: 'Medication' }));
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
    })
      .overrideProvider(MatDialog, { useValue: { open: openDialog } })
      .compileComponents();

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

    it('creates a new inventory and reloads the list', async () => {
      setup([Roles.admin]);

      const value = {
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      };

      await component['saveInventory'](value, null);

      expect(createInventory).toHaveBeenCalledWith({
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      });
      expect(getInventories).toHaveBeenCalledTimes(2);
      expect(component['savingInventory']()).toBe(false);
    });

    it('updates the inventory being edited instead of creating a new one', async () => {
      const stream = setup([Roles.admin]);
      stream.next(inventoryOne);

      component['editInventory'](inventoryOne);
      expect(openDialog).toHaveBeenCalledWith(
        ModalForm,
        expect.objectContaining({
          data: expect.objectContaining({
            initialValue: expect.objectContaining({ inventoryType: 'Medication' }),
          }),
        }),
      );

      const updated = { ...inventoryOne, inventoryName: 'Pharmacy (updated)' };
      updateInventory.mockReturnValue(of(updated));

      await component['saveInventory'](
        {
          inventoryName: 'Pharmacy',
          inventoryType: 'Medication',
          inventoryDescription: 'Prescription drugs',
        },
        inventoryOne,
      );

      expect(updateInventory).toHaveBeenCalledWith('inv-1', {
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      });
      expect(createInventory).not.toHaveBeenCalled();
      expect(getInventories).toHaveBeenCalledTimes(2);
      expect(component['savingInventory']()).toBe(false);
    });

    it('shows a form error when saving fails', async () => {
      setup([Roles.admin]);
      createInventory.mockReturnValue(throwError(() => new Error('failed')));

      const value = {
        inventoryName: 'Pharmacy',
        inventoryType: 'Medication',
        inventoryDescription: 'Prescription drugs',
      };
      await component['saveInventory'](value, null);

      expect(component['formError']()).toBe("Impossible de créer l'inventaire.");
    });

    it('deletes an inventory after confirmation and removes it locally', () => {
      const stream = setup([Roles.admin]);
      stream.next(inventoryOne);
      stream.next(inventoryTwo);

      component['deleteInventoryAction'](inventoryOne);
      dialogClosed.next({ value: {} });

      expect(deleteInventory).toHaveBeenCalledWith('inv-1');
      expect(component['inventories']()).toEqual([inventoryTwo]);
    });

    it('does not delete when the user cancels the confirmation', () => {
      const stream = setup([Roles.admin]);
      stream.next(inventoryOne);

      component['deleteInventoryAction'](inventoryOne);
      dialogClosed.next(undefined);

      expect(deleteInventory).not.toHaveBeenCalled();
      expect(component['inventories']()).toEqual([inventoryOne]);
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
      expect(openDialog).not.toHaveBeenCalled();

      component['deleteInventoryAction'](inventoryOne);
      expect(deleteInventory).not.toHaveBeenCalled();
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
