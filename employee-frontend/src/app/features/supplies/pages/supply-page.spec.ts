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
  const roles = signal<string[]>([Roles.admin]);
  const openDialog = vi.fn(() => ({
    afterClosed: () => dialogClosed.asObservable(),
  }));

  const supplyService = {
    getSupplies,
    createSupply,
    updateSupply,
    deleteSupply,
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
    expect(fixture.nativeElement.querySelector('.add-supply-button')?.textContent.trim()).toBe('Cancel');
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

    expect(component['supplyForm'].productPrice().errors()[0]?.message).toBe('Cost price is required');
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

    expect(openDialog).toHaveBeenCalledWith(component['deleteDialogTemplate'](), {
      data: supplyOne,
      ariaLabel: 'Delete supply',
    });

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
