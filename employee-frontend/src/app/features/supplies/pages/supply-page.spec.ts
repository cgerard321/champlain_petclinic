import { ComponentFixture, TestBed } from '@angular/core/testing';
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
  const hasRole = vi.fn<AuthState['hasRole']>();

  const supplyService = {
    getSupplies,
    createSupply,
    updateSupply,
    deleteSupply,
  };

  const authState = {
    hasRole,
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    getSupplies.mockReturnValue(of([supplyOne, supplyTwo]));
    createSupply.mockReturnValue(of(supplyOne));
    updateSupply.mockReturnValue(of(supplyOne));
    deleteSupply.mockReturnValue(of(void 0));

    hasRole.mockImplementation((role) => role === Roles.admin);

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
    }).compileComponents();

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
    expect(component['canManageSupplies']).toBe(true);

    expect(hasRole).toHaveBeenCalledWith(Roles.admin);
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

    expect(component['newSupply']()).toEqual({
      productName: '',
      productDescription: '',
      productPrice: 0,
      productQuantity: 1,
      productSalePrice: 0,
    });
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
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

    component['deleteSupply'](supplyOne);

    expect(confirmSpy).toHaveBeenCalledWith(`Delete ${supplyOne.productName}?`);

    expect(deleteSupply).not.toHaveBeenCalled();

    confirmSpy.mockRestore();
  });

  it('should delete a confirmed supply and reload the list', () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

    component['deleteSupply'](supplyOne);

    expect(deleteSupply).toHaveBeenCalledWith(inventoryId, supplyOne.productId);

    expect(getSupplies).toHaveBeenCalledTimes(2);

    confirmSpy.mockRestore();
  });

  it('should show an error when deleting a supply fails', () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

    deleteSupply.mockReturnValue(throwError(() => new Error('Delete failed')));

    component['deleteSupply'](supplyOne);

    expect(component['addError']()).toBe('Unable to delete supply.');

    confirmSpy.mockRestore();
  });

  it('should toggle the add supply form', () => {
    expect(component['showAddForm']()).toBe(false);

    component['toggleAddForm']();

    expect(component['showAddForm']()).toBe(true);

    component['toggleAddForm']();

    expect(component['showAddForm']()).toBe(false);
  });

  it('should format supply statuses for display', () => {
    expect(component['formatStatus']('OUT_OF_STOCK')).toBe('OUT OF STOCK');

    expect(component['formatStatus']('RE_ORDER')).toBe('RE ORDER');
  });
});
