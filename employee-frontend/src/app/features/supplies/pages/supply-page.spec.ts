import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Status } from '@features/supplies/models/status';
import { Supply } from '@features/supplies/models/supply';
import { SupplyService } from '@features/supplies/services/supply-service';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import { Roles } from '@shared/models/roles';

import { SupplyPage } from './supply-page';

describe('SupplyPage', () => {
  let fixture: ComponentFixture<SupplyPage>;
  let component: SupplyPage;
  let dialogClosed: Subject<boolean | undefined>;

  const inventoryId = 'test-inventory-id';

  const supplyOne: Supply = {
    productId: 'product-1',
    inventoryId,
    productName: 'Elastic Bandage',
    productDescription: 'Flexible wound support',
    productPrice: 8,
    productQuantity: 50,
    productSalePrice: 15,
    status: Status.AVAILABLE,
    lastUpdatedAt: '2026-09-26T12:00:00',
  };

  const supplyTwo: Supply = {
    productId: 'product-2',
    inventoryId,
    productName: 'Antiseptic Wipes',
    productDescription: 'Wipes for cleaning wounds',
    productPrice: 12,
    productQuantity: 20,
    productSalePrice: 18,
    status: Status.RE_ORDER,
    lastUpdatedAt: '2026-09-26T13:00:00',
  };

  const getSupplies = vi.fn<SupplyService['getSupplies']>();
  const createSupply = vi.fn<SupplyService['createSupply']>();
  const updateSupply = vi.fn<SupplyService['updateSupply']>();
  const deleteSupply = vi.fn<SupplyService['deleteSupply']>();
  const restockSupply = vi.fn<SupplyService['restockSupply']>();
  const closeDialog = vi.fn();
  const roles = signal<string[]>([Roles.admin]);
  const openDialog = vi.fn(() => ({
    afterClosed: () => dialogClosed.asObservable(),
    close: closeDialog,
    disableClose: false,
  }));

  const supplyService = {
    getSupplies,
    createSupply,
    updateSupply,
    deleteSupply,
    restockSupply,
  };

  const authState = {
    roles,
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    dialogClosed = new Subject<boolean | undefined>();
    roles.set([Roles.admin]);

    getSupplies.mockReturnValue(of([supplyOne, supplyTwo]));
    createSupply.mockReturnValue(of(supplyOne));
    updateSupply.mockReturnValue(of(supplyOne));
    deleteSupply.mockReturnValue(of(void 0));
    restockSupply.mockReturnValue(of(supplyTwo));

    await TestBed.configureTestingModule({
      imports: [SupplyPage],
      providers: [
        {
          provide: SupplyService,
          useValue: supplyService,
        },
        {
          provide: AuthState,
          useValue: authState,
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({
                inventoryId,
              }),
            },
          },
        },
      ],
    })
      .overrideProvider(MatDialog, { useValue: { open: openDialog } })
      .compileComponents();

    fixture = TestBed.createComponent(SupplyPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('restock', () => {
    const eligible = { ...supplyTwo, productQuantity: 5 };
    const submitEvent = (): Event => new Event('submit', { cancelable: true });

    it.each([Roles.admin, Roles.inventoryManager])(
      'allows %s to restock eligible supplies',
      (role) => {
        roles.set([role]);
        component['openRestock'](eligible);
        expect(openDialog).toHaveBeenCalled();
        expect(component['restockingSupply']()).toEqual(eligible);
      },
    );

    it.each([Roles.vet, Roles.customer, Roles.receptionist, ''])(
      'denies restock for role %s',
      (role) => {
        roles.set([role]);
        component['openRestock'](eligible);
        expect(openDialog).not.toHaveBeenCalled();
        expect(restockSupply).not.toHaveBeenCalled();
      },
    );

    it('only renders Restock for eligible statuses and removes it when access is revoked', () => {
      const buttons = (): HTMLButtonElement[] =>
        Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
      expect(
        buttons().filter((button) => button.textContent?.trim() === 'R\u00e9approvisionner'),
      ).toHaveLength(1);
      component['openRestock'](supplyOne);
      expect(openDialog).not.toHaveBeenCalled();
      roles.set([]);
      fixture.detectChanges();
      expect(
        buttons().filter((button) => button.textContent?.trim() === 'R\u00e9approvisionner'),
      ).toHaveLength(0);
    });

    it.each([Status.RE_ORDER, Status.OUT_OF_STOCK])('allows status %s', (status) => {
      component['openRestock']({ ...eligible, status });
      expect(openDialog).toHaveBeenCalled();
    });

    it('rechecks permissions on submit', () => {
      component['openRestock'](eligible);
      component['restockModel'].set({ quantity: 3 });
      roles.set([]);
      component['submitRestock'](submitEvent());
      expect(restockSupply).not.toHaveBeenCalled();
      expect(component['restockError']()).toContain('permission');
    });

    it.each([null, 0, -1, 1.5, NaN, Infinity, 2147483648])(
      'rejects invalid amount %s',
      (quantity) => {
        component['openRestock'](eligible);
        component['restockModel'].set({ quantity });
        component['submitRestock'](submitEvent());
        expect(restockSupply).not.toHaveBeenCalled();
        expect(component['restockError']()).toContain('integer');
      },
    );

    it('blocks duplicate submits and cancellation while pending, then applies the server response', () => {
      const pending = new Subject<Supply>();
      const updated = { ...eligible, productQuantity: 21, status: Status.AVAILABLE };
      restockSupply.mockReturnValue(pending);
      // A failed refresh must not turn this successful mutation into a failure.
      getSupplies.mockReturnValue(throwError(() => new Error('Refresh unavailable')));
      component['openRestock'](eligible);
      component['restockModel'].set({ quantity: 16 });
      component['submitRestock'](submitEvent());
      component['submitRestock'](submitEvent());
      component['cancelRestock']();
      expect(restockSupply).toHaveBeenCalledExactlyOnceWith(inventoryId, eligible.productId, 16);
      expect(closeDialog).not.toHaveBeenCalled();
      expect(openDialog.mock.results[0]?.value?.disableClose).toBe(true);
      pending.next(updated);
      pending.complete();
      component['submitRestock'](submitEvent());
      expect(restockSupply).toHaveBeenCalledTimes(1);
      expect(component['restockPending']()).toBe(true);
      dialogClosed.next(undefined);
      expect(component['supplies']().find((row) => row.productId === updated.productId)).toEqual(
        updated,
      );
      expect(component['restockPending']()).toBe(false);
      expect(component['restockError']()).toBe('');
      expect(closeDialog).toHaveBeenCalledOnce();
      expect(getSupplies).toHaveBeenCalledTimes(1);
    });

    it('preserves quantity and displays API errors, then permits retry', () => {
      restockSupply.mockReturnValue(
        throwError(() => ({ code: 400, message: 'Supply is no longer eligible.' })),
      );
      component['openRestock'](eligible);
      component['restockModel'].set({ quantity: 7 });
      component['submitRestock'](submitEvent());
      expect(component['restockModel']().quantity).toBe(7);
      expect(component['restockError']()).toBe('Supply is no longer eligible.');
      expect(component['restockPending']()).toBe(false);
      expect(openDialog.mock.results[0]?.value?.disableClose).toBe(false);
      expect(closeDialog).not.toHaveBeenCalled();
      restockSupply.mockReturnValue(of({ ...eligible, productQuantity: 12 }));
      component['submitRestock'](submitEvent());
      expect(closeDialog).toHaveBeenCalledOnce();
    });

    it.each([new Error('offline'), { code: 500, message: 'Audit unavailable' }])(
      'blocks retry after an uncertain stock outcome %s and allows cancel',
      (failure) => {
        component['openRestock'](eligible);
        component['cancelRestock']();
        expect(closeDialog).toHaveBeenCalledOnce();
        expect(restockSupply).not.toHaveBeenCalled();
        closeDialog.mockClear();
        component['restockModel'].set({ quantity: 2 });
        restockSupply.mockReturnValue(throwError(() => failure));
        component['submitRestock'](submitEvent());
        expect(component['restockError']()).toContain('Stock may have changed');
        component['submitRestock'](submitEvent());
        expect(restockSupply).toHaveBeenCalledTimes(1);
        expect(component['canRestock'](eligible)).toBe(false);
        expect(component['canRestock']({ ...eligible, productId: 'other-supply' })).toBe(true);
        expect(closeDialog).not.toHaveBeenCalled();
      },
    );
  });

  it('should combine filters dynamically and clear the controls without refetching', () => {
    const inputs = fixture.nativeElement.querySelectorAll(
      '.supply-filters input',
    ) as NodeListOf<HTMLInputElement>;
    const status = fixture.nativeElement.querySelector(
      '.supply-filters select',
    ) as HTMLSelectElement;
    inputs.item(0).value = '  WIPES  ';
    inputs.item(0).dispatchEvent(new Event('input'));
    inputs.item(1).value = 'CLEANING';
    inputs.item(1).dispatchEvent(new Event('input'));
    status.value = Status.RE_ORDER;
    status.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain('Antiseptic Wipes');

    status.value = Status.AVAILABLE;
    status.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No matching supplies');

    fixture.nativeElement.querySelector('.clear-filters-button').click();
    fixture.detectChanges();
    expect(inputs.item(0).value).toBe('');
    expect(inputs.item(1).value).toBe('');
    expect(status.value).toBe('');
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(getSupplies).toHaveBeenCalledTimes(1);
  });

  it('should filter out-of-stock supplies and recompute after supplies change', () => {
    component['filters'].set({
      productName: '',
      productDescription: '',
      status: Status.OUT_OF_STOCK,
    });
    expect(component['filteredSupplies']()).toEqual([]);
    const outOfStock = { ...supplyOne, status: Status.OUT_OF_STOCK, productQuantity: 0 };
    component['supplies'].set([outOfStock, supplyTwo]);
    expect(component['filteredSupplies']()).toEqual([outOfStock]);
  });

  it('should load supplies using the inventory id from the route', () => {
    expect(getSupplies).toHaveBeenCalledWith(inventoryId);

    expect(component['supplies']()).toEqual([supplyOne, supplyTwo]);

    expect(component['loading']()).toBe(false);
    expect(component['error']()).toBe(false);
  });

  it('should render supply data and actions in the shared table for an admin', () => {
    fixture.detectChanges();
    const table = fixture.nativeElement.querySelector('table') as HTMLTableElement;

    expect(table.getAttribute('aria-label')).toBe('Supplies in this inventory');
    expect(
      Array.from(table.querySelectorAll('thead th'), (cell) => cell.textContent?.trim()),
    ).toEqual(['Name', 'Description', 'Cost Price', 'Quantity', 'Status', 'Actions']);

    const rows = table.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0]?.textContent).toContain('Elastic Bandage');
    expect(rows[0]?.textContent).toContain('Flexible wound support');
    expect(rows[0]?.textContent).toContain('AVAILABLE');
    expect(rows[0]?.querySelector('.edit-button')).not.toBeNull();
    expect(rows[0]?.querySelector('.delete-button')).not.toBeNull();
  });

  it('should update management controls when roles change without reopening the page', () => {
    roles.set([]);
    fixture.detectChanges();

    const table = fixture.nativeElement.querySelector('table') as HTMLTableElement;
    const headers = Array.from(table.querySelectorAll('thead th'), (cell) =>
      cell.textContent?.trim(),
    );

    expect(table.querySelector('tbody tr')?.textContent).toContain('Elastic Bandage');
    expect(headers).not.toContain('Actions');
    expect(table.querySelector('.action-buttons')).toBeNull();
    expect(fixture.nativeElement.querySelector('.add-supply-button')).toBeNull();

    roles.set([Roles.inventoryManager]);
    fixture.detectChanges();

    expect(table.querySelector('thead')?.textContent).toContain('Actions');
    expect(table.querySelector('.action-buttons')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.add-supply-button')).not.toBeNull();
  });

  it('should remain in a loading state until the API responds', () => {
    const response = new Subject<Supply[]>();
    getSupplies.mockReturnValue(response);

    const loadingFixture = TestBed.createComponent(SupplyPage);
    const loadingComponent = loadingFixture.componentInstance;
    expect(loadingComponent['loading']()).toBe(true);

    response.next([supplyOne]);

    expect(loadingComponent['loading']()).toBe(false);
    expect(loadingComponent['supplies']()).toEqual([supplyOne]);
  });

  it('should show an error state when supplies cannot be loaded', () => {
    getSupplies.mockReturnValue(throwError(() => new Error('Backend unavailable')));

    const errorFixture = TestBed.createComponent(SupplyPage);
    const errorComponent = errorFixture.componentInstance;

    expect(errorComponent['error']()).toBe(true);
    expect(errorComponent['loading']()).toBe(false);
    expect(errorComponent['supplies']()).toEqual([]);
  });

  it('should allow an admin to manage supplies', () => {
    expect(component['canManageSupplies']()).toBe(true);
  });

  it('should hide an open form and preserve its draft when management access is removed', () => {
    component['editSupply'](supplyOne);
    component['newSupply'].update((draft) => ({ ...draft, productName: 'Updated Bandage' }));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.supply-form')).not.toBeNull();

    roles.set([]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.supply-form')).toBeNull();
    expect(component['showAddForm']()).toBe(true);
    expect(component['editingSupplyId']()).toBe(supplyOne.productId);
    expect(component['newSupply']().productName).toBe('Updated Bandage');

    roles.set([Roles.admin]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.supply-form')).not.toBeNull();
    const nameInput = fixture.nativeElement.querySelector('#productName') as HTMLInputElement;
    expect(nameInput.value).toBe('Updated Bandage');
    expect(fixture.nativeElement.querySelector('.add-supply-button')?.textContent.trim()).toBe(
      'Cancel',
    );
  });

  it('should populate the form when editing a supply', () => {
    component['editSupply'](supplyOne);

    expect(component['editingSupplyId']()).toBe(supplyOne.productId);

    expect(component['newSupply']()).toEqual({
      productName: supplyOne.productName,
      productDescription: supplyOne.productDescription,
      productPrice: supplyOne.productPrice,
      productQuantity: supplyOne.productQuantity,
      productSalePrice: supplyOne.productSalePrice,
    });

    expect(component['showAddForm']()).toBe(true);
  });

  it('should clear the previous error and touched state when editing another supply', () => {
    component['editSupply'](supplyOne);
    component['addError'].set('Unable to update supply.');
    component['supplyForm'].productName().markAsTouched();
    expect(component['supplyForm'].productName().touched()).toBe(true);

    component['editSupply'](supplyTwo);

    expect(component['editingSupplyId']()).toBe(supplyTwo.productId);
    expect(component['newSupply']().productName).toBe(supplyTwo.productName);
    expect(component['addError']()).toBe('');
    expect(component['supplyForm'].productName().touched()).toBe(false);
  });

  it('should create a new supply and reload the supply list', async () => {
    const newSupply = {
      productName: 'Gauze Pads',
      productDescription: 'Absorbent wound pads',
      productPrice: 10,
      productQuantity: 25,
      productSalePrice: 16,
    };

    const createdSupply: Supply = {
      productId: 'product-3',
      inventoryId,
      ...newSupply,
      status: Status.AVAILABLE,
      lastUpdatedAt: '2026-09-26T14:00:00',
    };

    createSupply.mockReturnValue(of(createdSupply));

    component['newSupply'].set(newSupply);
    component['showAddForm'].set(true);
    // Simulate a touched field to verify that a successful save clears its validation state.
    component['supplyForm'].productName().markAsTouched();
    expect(component['supplyForm'].productName().touched()).toBe(true);

    const event = new Event('submit', {
      cancelable: true,
    });

    component['addSupply'](event);

    await fixture.whenStable();

    expect(event.defaultPrevented).toBe(true);

    expect(createSupply).toHaveBeenCalledWith(inventoryId, newSupply);

    expect(getSupplies).toHaveBeenCalledTimes(2);

    expect(component['showAddForm']()).toBe(false);
    expect(component['editingSupplyId']()).toBe(null);
    expect(component['addingSupply']()).toBe(false);
    expect(component['supplyForm'].productName().touched()).toBe(false);

    expect(component['newSupply']()).toEqual({
      productName: '',
      productDescription: '',
      productPrice: 0,
      productQuantity: 1,
      productSalePrice: 0,
    });
  });

  it('should not save a supply after management access is removed', async () => {
    component['newSupply'].set({
      productName: 'Gauze Pads',
      productDescription: 'Absorbent wound pads',
      productPrice: 10,
      productQuantity: 25,
      productSalePrice: 16,
    });

    roles.set([]);
    component['addSupply'](new Event('submit', { cancelable: true }));
    await fixture.whenStable();

    expect(createSupply).not.toHaveBeenCalled();
    expect(updateSupply).not.toHaveBeenCalled();
  });

  it('should not create a supply when numeric fields are empty', async () => {
    component['newSupply'].set({
      productName: 'Gauze Pads',
      productDescription: 'Absorbent wound pads',
      productPrice: 10,
      productQuantity: 25,
      productSalePrice: 16,
    });
    component['openAddForm']();
    fixture.detectChanges();

    for (const id of ['productPrice', 'productQuantity', 'productSalePrice']) {
      const input = fixture.nativeElement.querySelector(`#${id}`) as HTMLInputElement;
      input.value = '';
      input.dispatchEvent(new Event('input', { bubbles: true }));
    }

    await fixture.whenStable();

    expect(component['supplyForm'].productPrice().errors()[0]?.message).toBe(
      'Cost price is required',
    );
    expect(component['supplyForm'].productQuantity().errors()[0]?.message).toBe(
      'Quantity is required',
    );
    expect(component['supplyForm'].productSalePrice().errors()[0]?.message).toBe(
      'Sale price is required',
    );

    component['addSupply'](new Event('submit', { cancelable: true }));
    await fixture.whenStable();

    expect(createSupply).not.toHaveBeenCalled();
  });

  it('should update the selected supply instead of creating a new one', async () => {
    component['editSupply'](supplyOne);

    const updatedSupplyData = {
      productName: 'Updated Bandage',
      productDescription: 'Updated description',
      productPrice: 9,
      productQuantity: 60,
      productSalePrice: 17,
    };

    component['newSupply'].set(updatedSupplyData);

    const updatedSupply: Supply = {
      ...supplyOne,
      ...updatedSupplyData,
    };

    updateSupply.mockReturnValue(of(updatedSupply));

    const event = new Event('submit', {
      cancelable: true,
    });
    component['addSupply'](event);

    await fixture.whenStable();

    expect(updateSupply).toHaveBeenCalledWith(inventoryId, supplyOne.productId, updatedSupplyData);
    expect(createSupply).not.toHaveBeenCalled();
    expect(getSupplies).toHaveBeenCalledTimes(2);
    expect(component['editingSupplyId']()).toBe(null);
  });

  it('should show an error when creating a supply fails', async () => {
    const newSupply = {
      productName: 'Gauze Pads',
      productDescription: 'Absorbent wound pads',
      productPrice: 10,
      productQuantity: 25,
      productSalePrice: 16,
    };

    component['newSupply'].set(newSupply);

    createSupply.mockReturnValue(throwError(() => new Error('Create failed')));

    component['addSupply'](
      new Event('submit', {
        cancelable: true,
      }),
    );

    await fixture.whenStable();

    expect(component['addError']()).toBe('Unable to add supply.');

    expect(component['addingSupply']()).toBe(false);
  });

  it('should not delete a supply when the user cancels confirmation', () => {
    component['deleteSupply'](supplyOne);

    expect(openDialog).toHaveBeenCalledWith(
      ModalForm,
      expect.objectContaining({
        data: expect.objectContaining({
          title: "Supprimer l'article",
          message: 'Voulez-vous vraiment supprimer Bandage \u00e9lastique ?',
          fields: [],
          destructive: true,
        }),
        ariaLabel: "Supprimer l'article",
      }),
    );

    dialogClosed.next(false);
    dialogClosed.complete();

    expect(deleteSupply).not.toHaveBeenCalled();
  });

  it('should not delete after management access is removed while confirmation is open', () => {
    component['deleteSupply'](supplyOne);

    roles.set([]);
    dialogClosed.next(true);
    dialogClosed.complete();

    expect(deleteSupply).not.toHaveBeenCalled();
  });

  it('should delete a confirmed supply and reload the list', () => {
    component['deleteSupply'](supplyOne);

    expect(deleteSupply).not.toHaveBeenCalled();

    dialogClosed.next(true);
    dialogClosed.complete();

    expect(deleteSupply).toHaveBeenCalledWith(inventoryId, supplyOne.productId);

    expect(getSupplies).toHaveBeenCalledTimes(2);
  });

  it('should keep the table visible while refreshing after a deletion', () => {
    const table = fixture.nativeElement.querySelector('table');
    const refresh = new Subject<Supply[]>();
    getSupplies.mockReturnValue(refresh);

    component['deleteSupply'](supplyOne);
    dialogClosed.next(true);
    fixture.detectChanges();

    expect(getSupplies).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.querySelector('table')).toBe(table);
    refresh.complete();
  });

  it('should show an error when deleting a supply fails', () => {
    deleteSupply.mockReturnValue(throwError(() => new Error('Delete failed')));

    component['deleteSupply'](supplyOne);

    dialogClosed.next(true);
    dialogClosed.complete();

    expect(component['deleteError']()).toBe('Unable to delete supply.');
  });

  it('should open and cancel the add supply form', () => {
    expect(component['showAddForm']()).toBe(false);

    component['openAddForm']();

    expect(component['showAddForm']()).toBe(true);

    component['cancelAddForm']();

    expect(component['showAddForm']()).toBe(false);
  });

  it('should start a blank add form after cancelling an edit', () => {
    component['editSupply'](supplyOne);
    component['addError'].set('Unable to update supply.');

    component['cancelAddForm']();
    component['openAddForm']();
    fixture.detectChanges();

    expect(component['editingSupplyId']()).toBeNull();
    expect(component['newSupply']()).toEqual({
      productName: '',
      productDescription: '',
      productPrice: 0,
      productQuantity: 1,
      productSalePrice: 0,
    });
    expect(component['addError']()).toBe('');
    expect(fixture.nativeElement.querySelector('.supply-form h2')?.textContent.trim()).toBe(
      'Add Supply',
    );
  });

  it('should format supply statuses for display', () => {
    expect(component['formatStatus']('OUT_OF_STOCK')).toBe('OUT OF STOCK');

    expect(component['formatStatus']('RE_ORDER')).toBe('RE ORDER');
  });
});

describe('SupplyPage restock modal rendering', () => {
  it('shows the selected supply, preserves input on failure and closes after a successful retry', async () => {
    const supply: Supply = {
      productId: 'low-stock',
      inventoryId: 'inventory',
      productName: 'Bandages',
      productDescription: 'Sterile bandages',
      productQuantity: 0,
      productPrice: 5,
      productSalePrice: 10,
      status: Status.OUT_OF_STOCK,
      lastUpdatedAt: '',
    };
    const pending = new Subject<Supply>();
    const restock = vi.fn().mockReturnValue(pending);
    await TestBed.configureTestingModule({
      imports: [SupplyPage],
      providers: [
        {
          provide: SupplyService,
          useValue: { getSupplies: () => of([supply]), restockSupply: restock },
        },
        { provide: AuthState, useValue: { roles: signal([Roles.inventoryManager]) } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ inventoryId: 'inventory' }) } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(SupplyPage);
    fixture.detectChanges();
    const button = (
      Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[]
    ).find((candidate) => candidate.textContent?.trim() === 'R\u00e9approvisionner');
    button?.click();
    fixture.detectChanges();
    await fixture.whenStable();
    const modal = document.querySelector('mat-dialog-container')!;
    expect(modal.textContent).toContain('Bandages');
    expect(modal.textContent).toContain('Current quantity: 0');
    const input = modal.querySelector<HTMLInputElement>('#restockQuantity')!;
    input.value = '3';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    const form = modal.querySelector('form')!;
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(restock).toHaveBeenCalledExactlyOnceWith('inventory', 'low-stock', 3);
    expect(input.readOnly).toBe(true);
    expect(modal.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBe(true);
    pending.error({ code: 400, message: 'Stock changed. Refresh and try again.' });
    fixture.detectChanges();
    expect(modal.querySelector('[role="alert"]')?.textContent).toContain('Stock changed');
    expect(input.value).toBe('3');
    expect(input.readOnly).toBe(false);
    const updated = { ...supply, productQuantity: 3, status: Status.RE_ORDER };
    restock.mockReturnValue(of(updated));
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance['supplies']()).toEqual([updated]);
    await vi.waitFor(() => expect(document.querySelector('mat-dialog-container')).toBeNull());
  });
});
