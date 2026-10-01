import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { describe, it, expect, vi } from 'vitest';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

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
  let roles: string[];

  const mainInventory: Inventory = {
    inventoryId: 'inv-1',
    inventoryName: 'Main',
    inventoryType: 'Pharmacy',
    inventoryDescription: 'Main inventory',
    important: false,
  };

  const inventoryService = {
    getInventories: vi.fn(),
    getQuantity: vi.fn(),
    updateImportantStatus: vi.fn(),
  };

  const createComponent = (): void => {
    fixture = TestBed.createComponent(InventoryList);
    fixture.detectChanges();
  };

  const stars = (): HTMLButtonElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll('.favorite-button'));

  const checkbox = (): HTMLInputElement =>
    fixture.nativeElement.querySelector('input[type="checkbox"]');

  const setFavoritesOnly = (checked: boolean): void => {
    const input = checkbox();
    input.checked = checked;
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
  };

  function setup(roles: Roles[]): Subject<Inventory> {
    vi.clearAllMocks();
    roles = ['ADMIN'];

    const stream = new Subject<Inventory>();
    getInventories.mockReturnValue(stream);
    getQuantity.mockReturnValue(of(10));
    createInventory.mockReturnValue(of(inventoryOne));
    updateInventory.mockReturnValue(of(inventoryOne));
    deleteInventory.mockReturnValue(of(void 0));
    inventoryService.getInventories.mockReturnValue(of(mainInventory));
    inventoryService.getQuantity.mockReturnValue(of(10));
    inventoryService.updateImportantStatus.mockReturnValue(of(undefined));

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
        {
          provide: InventoryService,
          useValue: inventoryService,
        },
        {
          provide: AuthState,
          useValue: { hasRole: (role: string) => roles.includes(role) },
        },
        {
          provide: ActivatedRoute,
          useValue: {},
        },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    // Act
    createComponent();

    // Assert
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load inventories without the favorite filter on initialization', () => {
    // Act
    createComponent();

    // Assert
    expect(inventoryService.getInventories).toHaveBeenCalledWith(false);
    expect(fixture.nativeElement.textContent).toContain('Main');
    expect(fixture.nativeElement.textContent).toContain('10 items in stock');
  });

  it('should reload from the backend when the favorites filter is turned on', () => {
    // Act
    createComponent();
    setFavoritesOnly(true);

    // Assert
    expect(inventoryService.getInventories).toHaveBeenLastCalledWith(true);
  });

  it('should drop the previous inventory and quantity requests when the filter changes', () => {
    // Arrange
    const firstStream = new Subject<Inventory>();
    const quantityRequest = new Subject<number>();

    inventoryService.getInventories
      .mockReturnValueOnce(firstStream)
      .mockReturnValueOnce(new Subject<Inventory>());
    inventoryService.getQuantity.mockReturnValue(quantityRequest);

    // Act
    createComponent();
    firstStream.next(mainInventory);
    fixture.detectChanges();

    // Assert
    expect(firstStream.observed).toBe(true);
    expect(quantityRequest.observed).toBe(true);

    // Act
    setFavoritesOnly(true);

    // Assert
    expect(firstStream.observed).toBe(false);
    expect(quantityRequest.observed).toBe(false);
  });

  it('should show the favorites-specific empty message when the filter returns nothing', () => {
    // Arrange
    inventoryService.getInventories.mockReturnValue(EMPTY);

    // Act
    createComponent();

    // Assert
    expect(fixture.nativeElement.textContent).toContain('No inventories found.');

    // Act
    setFavoritesOnly(true);

    // Assert
    expect(fixture.nativeElement.textContent).toContain('No favorite inventories found.');
  });

  it('should mark an inventory as favorite and back again', () => {
    // Act
    createComponent();
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(inventoryService.updateImportantStatus).toHaveBeenCalledWith('inv-1', true);
    expect(stars()[0]?.getAttribute('aria-pressed')).toBe('true');

    // Act
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(inventoryService.updateImportantStatus).toHaveBeenLastCalledWith('inv-1', false);
    expect(stars()[0]?.getAttribute('aria-pressed')).toBe('false');
  });

  it('should keep the star unchanged and report a local error when the save fails', () => {
    // Arrange
    inventoryService.updateImportantStatus.mockReturnValue(throwError(() => new Error('failed')));

    // Act
    createComponent();
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(stars()[0]?.getAttribute('aria-pressed')).toBe('false');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Could not update the favorite for Main.',
    );
  });

  it('should disable the star while its save is pending', () => {
    // Arrange
    inventoryService.updateImportantStatus.mockReturnValue(new Subject<void>());

    // Act
    createComponent();
    stars()[0]?.click();
    fixture.detectChanges();
    stars()[0]?.click();

    // Assert
    expect(stars()[0]?.disabled).toBe(true);
    expect(inventoryService.updateImportantStatus).toHaveBeenCalledTimes(1);
  });

  it('should block filter changes while a favorite save is pending', () => {
    // Arrange
    inventoryService.updateImportantStatus.mockReturnValue(new Subject<void>());

    // Act
    createComponent();
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(checkbox().disabled).toBe(true);

    // Act
    setFavoritesOnly(true);

    // Assert
    expect(inventoryService.getInventories).toHaveBeenCalledTimes(1);
  });

  it('should remove an inventory from the list when it is unfavorited in favorites-only mode', () => {
    // Arrange
    inventoryService.getInventories.mockReturnValue(of({ ...mainInventory, important: true }));

    // Act
    createComponent();
    setFavoritesOnly(true);
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(inventoryService.updateImportantStatus).toHaveBeenCalledWith('inv-1', false);
    expect(stars()).toHaveLength(0);
    expect(fixture.nativeElement.textContent).toContain('No favorite inventories found.');
  });

  it('should not let users without the inventory roles change favorites', () => {
    // Arrange
    roles = ['VET'];

    // Act
    createComponent();
    stars()[0]?.click();

    // Assert
    expect(stars()[0]?.disabled).toBe(true);
    expect(inventoryService.updateImportantStatus).not.toHaveBeenCalled();
    expect(checkbox().disabled).toBe(false);
  });
});
