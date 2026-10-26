import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Subject, of, throwError } from 'rxjs';
import { afterEach, describe, expect, it, vi } from 'vitest';

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

  const openDialog = vi.fn();
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
    vi.resetAllMocks();
    openDialog.mockReturnValue({ afterClosed: () => of(undefined) });
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
        { provide: MatDialog, useValue: { open: openDialog } },
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

  afterEach(() => {
    fixture?.destroy();
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  const stars = (): HTMLButtonElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll('.favorite-button'));
  const checkbox = (): HTMLInputElement =>
    fixture.nativeElement.querySelector('input[type="checkbox"]');
  const setFavoritesOnly = (checked: boolean): void => {
    checkbox().checked = checked;
    checkbox().dispatchEvent(new Event('change'));
    vi.advanceTimersByTime(300);
    fixture.detectChanges();
  };
  const loadInventory = (stream: Subject<Inventory>, inventory = inventoryOne): void => {
    stream.next(inventory);
    stream.complete();
    fixture.detectChanges();
  };
  const enableDialogs = (): void => {
    component['inventoryTypes'].set([{ typeId: 'type-1', type: 'Medication' }]);
  };
  const formValue = {
    inventoryName: 'Pharmacy',
    inventoryType: 'Medication',
    inventoryDescription: 'Prescription drugs',
  };
  const dialogResult = (value = formValue): void => {
    openDialog.mockReturnValue({ afterClosed: () => of({ value }) });
  };
  const settleDialog = async (): Promise<void> => {
    await fixture.whenStable();
    fixture.detectChanges();
  };

  describe.each([Roles.admin, Roles.inventoryManager])('full-access role (%s)', (role) => {
    it('exposes create/update/delete capabilities', () => {
      setup([role]);
      expect(component['can']().canCreateInventory).toBe(true);
      expect(component['can']().canUpdateInventory).toBe(true);
      expect(component['can']().canDeleteInventory).toBe(true);
    });

    it('creates a new inventory and reflects it after reloading', async () => {
      setup([role]);
      enableDialogs();
      dialogResult();
      getInventories.mockReturnValue(of(inventoryOne));
      component['openAddDialog']();
      await settleDialog();
      expect(createInventory).toHaveBeenCalledWith(formValue);
      expect(updateInventory).not.toHaveBeenCalled();
      expect(component['inventories']()).toEqual([inventoryOne]);
      expect(component['savingInventory']()).toBe(false);
    });

    it('updates the inventory being edited instead of creating a new one', async () => {
      loadInventory(setup([role]));
      enableDialogs();
      const updated = { ...inventoryOne, inventoryName: 'Pharmacy (updated)' };
      dialogResult({ ...formValue, inventoryName: updated.inventoryName });
      getInventories.mockReturnValue(of(updated));
      component['editInventory'](inventoryOne);
      await settleDialog();
      expect(updateInventory).toHaveBeenCalledWith('inv-1', {
        ...formValue,
        inventoryName: updated.inventoryName,
      });
      expect(createInventory).not.toHaveBeenCalled();
      expect(component['inventories']()).toEqual([updated]);
    });

    it('shows a form error when saving fails', async () => {
      setup([role]);
      enableDialogs();
      dialogResult();
      createInventory.mockReturnValue(throwError(() => new Error('failed')));
      component['openAddDialog']();
      await settleDialog();
      expect(createInventory).toHaveBeenCalledWith(formValue);
      expect(component['formError']()).toBe("Impossible de cr\u00e9er l'inventaire.");
      expect(component['savingInventory']()).toBe(false);
    });

    it('deletes an inventory after confirmation and removes it locally', () => {
      const stream = setup([role]);
      stream.next(inventoryOne);
      stream.next(inventoryTwo);
      stream.complete();
      dialogResult();
      component['deleteInventoryAction'](inventoryOne);
      expect(deleteInventory).toHaveBeenCalledWith('inv-1');
      expect(component['inventories']()).toEqual([inventoryTwo]);
    });

    it('does not delete when the user cancels the confirmation', () => {
      loadInventory(setup([role]));
      component['deleteInventoryAction'](inventoryOne);
      expect(openDialog).toHaveBeenCalledOnce();
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

    it('create, edit and delete actions are no-ops without the capability', () => {
      loadInventory(setup([Roles.vet]));
      component['openAddDialog']();
      component['editInventory'](inventoryOne);
      component['deleteInventoryAction'](inventoryOne);
      expect(openDialog).not.toHaveBeenCalled();
      expect(createInventory).not.toHaveBeenCalled();
      expect(updateInventory).not.toHaveBeenCalled();
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

  describe('favorites', () => {
    it('loads inventories without the favorites filter initially', () => {
      loadInventory(setup());
      expect(getInventories).toHaveBeenCalledWith(
        expect.objectContaining({ importantOnly: false }),
        0,
        8,
      );
      expect(checkbox().checked).toBe(false);
    });

    it.each([Roles.vet, Roles.admin, Roles.inventoryManager])(
      'lets %s favorite and unfavorite with synchronized icons and ARIA state',
      (role) => {
        loadInventory(setup([role]));
        const star = (): HTMLButtonElement => stars()[0]!;
        expect(star().disabled).toBe(false);
        expect(star().getAttribute('aria-pressed')).toBe('false');
        expect(star().getAttribute('aria-label')).toBe('Ajouter Pharmacy aux favoris');
        expect(star().querySelector('mat-icon')?.textContent?.trim()).toBe('star_border');
        star().click();
        fixture.detectChanges();
        expect(updateImportantStatus).toHaveBeenCalledWith('inv-1', true);
        expect(star().getAttribute('aria-pressed')).toBe('true');
        expect(star().getAttribute('aria-label')).toBe('Retirer Pharmacy des favoris');
        expect(star().querySelector('mat-icon')?.textContent?.trim()).toBe('star');
        star().click();
        fixture.detectChanges();
        expect(updateImportantStatus).toHaveBeenLastCalledWith('inv-1', false);
        expect(star().getAttribute('aria-pressed')).toBe('false');
        expect(star().getAttribute('aria-label')).toBe('Ajouter Pharmacy aux favoris');
        expect(star().querySelector('mat-icon')?.textContent?.trim()).toBe('star_border');
      },
    );

    it.each([Roles.vet, Roles.admin, Roles.inventoryManager])(
      'synchronizes %s favorites-only results after both toggles',
      (role) => {
        vi.useFakeTimers();
        loadInventory(setup([role]));
        let important = false;
        // Main collects completed pages and filters locally, including favorites.
        getInventories.mockImplementation(() => of({ ...inventoryOne, important }, inventoryTwo));
        updateImportantStatus.mockImplementation((_id, value: boolean) => {
          important = value;
          return of(undefined);
        });
        setFavoritesOnly(true);
        expect(stars()).toHaveLength(0);
        expect(component['noResultsMessage']()).toBe(component['noFavoritesLabel']);
        setFavoritesOnly(false);
        expect(stars()).toHaveLength(2);
        stars()[0]!.click();
        fixture.detectChanges();
        expect(updateImportantStatus).toHaveBeenCalledWith('inv-1', true);
        setFavoritesOnly(true);
        expect(getInventories).toHaveBeenLastCalledWith({}, 0, 50);
        expect(component['filters']().importantOnly).toBe(true);
        expect(component['inventories']()).toEqual([{ ...inventoryOne, important: true }]);
        expect(stars()).toHaveLength(1);
        stars()[0]!.click();
        fixture.detectChanges();
        expect(updateImportantStatus).toHaveBeenLastCalledWith('inv-1', false);
        expect(stars()).toHaveLength(0);
        expect(component['noResultsMessage']()).toBe(component['noFavoritesLabel']);
      },
    );

    it.each([Roles.receptionist, Roles.customer, ''])(
      'blocks direct favorite updates for unauthorized role %j',
      (role) => {
        setup(role ? [role] : []);
        // These roles have no inventory access, so no star should be rendered at all.
        expect(stars()).toHaveLength(0);
        component['toggleFavorite'](inventoryOne);
        expect(updateImportantStatus).not.toHaveBeenCalled();
      },
    );

    it('blocks favorite updates while inventories are loading', () => {
      setup([Roles.vet]);
      expect(component['isLoading']()).toBe(true);
      component['toggleFavorite'](inventoryOne);
      expect(updateImportantStatus).not.toHaveBeenCalled();
    });

    it('disables only the saving star and blocks duplicate saves until completion', () => {
      const stream = setup([Roles.vet]);
      stream.next(inventoryOne);
      stream.next(inventoryTwo);
      stream.complete();
      fixture.detectChanges();
      const save = new Subject<void>();
      updateImportantStatus.mockReturnValue(save);
      stars()[0]!.click();
      fixture.detectChanges();
      expect(stars()[0]!.disabled).toBe(true);
      expect(stars()[1]!.disabled).toBe(false);
      stars()[0]!.click();
      component['toggleFavorite'](inventoryOne);
      expect(updateImportantStatus).toHaveBeenCalledTimes(1);
      save.next();
      save.complete();
      fixture.detectChanges();
      expect(stars()[0]!.disabled).toBe(false);
      expect(stars()[0]!.getAttribute('aria-pressed')).toBe('true');
    });

    it('blocks filter changes while a favorite save is pending', () => {
      vi.useFakeTimers();
      loadInventory(setup([Roles.vet]));
      updateImportantStatus.mockReturnValue(new Subject<void>());
      stars()[0]!.click();
      fixture.detectChanges();
      expect(checkbox().disabled).toBe(true);
      setFavoritesOnly(true);
      expect(component['filters']().importantOnly).toBe(false);
      expect(getInventories).toHaveBeenCalledTimes(1);
    });

    it('retains the star and displays an error after a failed save, allowing retry', () => {
      loadInventory(setup([Roles.vet]));
      updateImportantStatus.mockReturnValue(throwError(() => new Error('failed')));
      stars()[0]!.click();
      fixture.detectChanges();
      expect(stars()[0]!.getAttribute('aria-pressed')).toBe('false');
      expect(stars()[0]!.disabled).toBe(false);
      expect(
        fixture.nativeElement.querySelector('.favorite-error[role="alert"]').textContent,
      ).toContain('Impossible de modifier le favori pour Pharmacy.');
      updateImportantStatus.mockReturnValue(of(undefined));
      stars()[0]!.click();
      fixture.detectChanges();
      expect(stars()[0]!.getAttribute('aria-pressed')).toBe('true');
      expect(fixture.nativeElement.querySelector('.favorite-error')).toBeNull();
    });

    it('cancels old inventory and quantity requests on a filter change', () => {
      vi.useFakeTimers();
      const stream = setup([Roles.vet]);
      const quantityRequest = new Subject<number>();
      getQuantity.mockReturnValue(quantityRequest);
      stream.next(inventoryOne);
      fixture.detectChanges();
      expect(stream.observed).toBe(true);
      expect(quantityRequest.observed).toBe(true);
      getQuantity.mockReturnValue(of(10));
      getInventories.mockReturnValue(of({ ...inventoryOne, important: true }));
      setFavoritesOnly(true);
      expect(stream.observed).toBe(false);
      expect(quantityRequest.observed).toBe(false);
      expect(component['inventories']()).toEqual([{ ...inventoryOne, important: true }]);
    });
  });
});
