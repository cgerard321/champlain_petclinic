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
import { ModalFormData, ModalFormResult } from '@shared/components/modal-form/modal-form-models';
import { SupplySummaryDialog } from '@shared/components/supply-summary-dialog/supply-summary-dialog';
import { Roles } from '@shared/models/roles';

import { SupplyPage } from './supply-page';

describe('SupplyPage', () => {
  let fixture: ComponentFixture<SupplyPage>;
  let component: SupplyPage;
  let dialogClosed: Subject<ModalFormResult<Record<string, unknown>> | undefined>;

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
  const openDialog = vi.fn(
    (dialogComponent: unknown, config?: { data?: ModalFormData<Record<string, unknown>> }) => {
      expect(dialogComponent).toBeDefined();
      expect(config).toBeDefined();
      return {
        afterClosed: () => dialogClosed.asObservable(),
      };
    },
  );

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
    dialogClosed = new Subject<ModalFormResult<Record<string, unknown>> | undefined>();
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

  it('should combine filters dynamically and clear the controls without refetching', () => {
    const inputs = fixture.nativeElement.querySelectorAll(
      '.supply-filters input',
    ) as NodeListOf<HTMLInputElement>;
    const status = fixture.nativeElement.querySelector(
      '.supply-filters select',
    ) as HTMLSelectElement;
    inputs.item(0).value = '  WIPES  ';
    inputs.item(0).dispatchEvent(new Event('input'));
    inputs.item(2).value = 'CLEANING';
    inputs.item(2).dispatchEvent(new Event('input'));
    status.value = Status.RE_ORDER;
    status.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain(
      'Lingettes antiseptiques',
    );

    status.value = Status.AVAILABLE;
    status.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucun article correspondant');

    fixture.nativeElement.querySelector('.clear-filters-button').click();
    fixture.detectChanges();
    expect(inputs.item(0).value).toBe('');
    expect(inputs.item(2).value).toBe('');
    expect(status.value).toBe('');
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(getSupplies).toHaveBeenCalledTimes(1);
  });

  it('should filter out-of-stock supplies and recompute after supplies change', () => {
    component['filters'].set({
      productName: '',
      productDescription: '',
      supplierName: '',
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

    expect(table.getAttribute('aria-label')).toBe('Articles dans cet inventaire');
    expect(
      Array.from(table.querySelectorAll('thead th'), (cell) => cell.textContent?.trim()),
    ).toEqual([
      'Nom',
      'Fournisseur',
      'Description',
      'Prix coûtant (CAD)',
      'Quantité',
      "Date d'expiration",
      'État',
      'Actions',
    ]);

    const rows = table.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0]?.textContent).toContain('Bandage élastique');
    expect(rows[0]?.textContent).toContain('Flexible wound support');
    expect(rows[0]?.textContent).toContain('Disponible');
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

    expect(table.querySelector('tbody tr')?.textContent).toContain('Bandage élastique');
    expect(headers).toContain('Actions');
    expect(table.querySelector('.summary-button')).not.toBeNull();
    expect(table.querySelector('.edit-button:not(.summary-button)')).toBeNull();
    expect(table.querySelector('.delete-button')).toBeNull();
    expect(table.querySelector('.history-button.desktop-action-button')).toBeNull();
    expect(fixture.nativeElement.querySelector('.add-supply-button')).toBeNull();

    roles.set([Roles.inventoryManager]);
    fixture.detectChanges();

    expect(table.querySelector('thead')?.textContent).toContain('Actions');
    expect(table.querySelector('.action-buttons')).not.toBeNull();
    expect(table.querySelector('.summary-button')).not.toBeNull();
    expect(table.querySelector('.edit-button:not(.summary-button)')).not.toBeNull();
    expect(table.querySelector('.delete-button')).not.toBeNull();
    expect(table.querySelector('.history-button.desktop-action-button')).not.toBeNull();
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

  it('should open the summary for all users and limit its actions to their list permissions', () => {
    roles.set([Roles.vet]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.summary-button')).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector(
        '.edit-button.desktop-action-button:not(.summary-button)',
      ),
    ).toBeNull();
    expect(fixture.nativeElement.querySelector('.delete-button')).toBeNull();
    expect(fixture.nativeElement.querySelector('.history-button.desktop-action-button')).toBeNull();

    component['openSupplySummary'](supplyOne);

    expect(openDialog).toHaveBeenCalledWith(
      SupplySummaryDialog,
      expect.objectContaining({
        data: expect.objectContaining({
          supply: supplyOne,
          canManage: false,
          canConsume: true,
          consumeDisabled: false,
        }),
      }),
    );
  });

  it('should show management actions in the summary only for inventory managers and admins', () => {
    roles.set([Roles.inventoryManager]);

    component['openSupplySummary'](supplyOne);

    expect(openDialog).toHaveBeenCalledWith(
      SupplySummaryDialog,
      expect.objectContaining({
        data: expect.objectContaining({
          canManage: true,
          canConsume: false,
        }),
      }),
    );
  });

  function dialogData(): ModalFormData<Record<string, unknown>> {
    const data = openDialog.mock.calls.at(-1)?.[1]?.data;
    expect(data).toBeDefined();
    return data!;
  }

  const newSupply = {
    productName: 'Gauze Pads',
    supplierName: 'Supplier',
    productDescription: 'Absorbent wound pads',
    productPrice: 10,
    productQuantity: 25,
    productSalePrice: 16,
    expirationDate: '',
    photo: { action: 'keep' },
  };

  it('should not save an open dialog after management access is removed', async () => {
    component['editSupply'](supplyOne);
    roles.set([]);
    dialogClosed.next({ value: newSupply });
    await fixture.whenStable();
    expect(createSupply).not.toHaveBeenCalled();
    expect(updateSupply).not.toHaveBeenCalled();
  });

  it('should populate the modal when editing a supply', () => {
    component['editSupply'](supplyOne);
    expect(openDialog).toHaveBeenCalledWith(ModalForm, expect.any(Object));
    expect(dialogData().initialValue).toEqual({
      productName: component['localizedSupplyName'](supplyOne.productName),
      supplierName: '',
      productDescription: component['localizedSupplyDescription'](supplyOne.productDescription),
      productPrice: supplyOne.productPrice,
      productQuantity: supplyOne.productQuantity,
      productSalePrice: supplyOne.productSalePrice,
      expirationDate: '',
    });
  });

  it('should clear the previous error and use fresh modal data when editing another supply', () => {
    component['editSupply'](supplyOne);
    const firstData = dialogData();
    component['addError'].set('Previous error');
    component['editSupply'](supplyTwo);
    expect(dialogData()).not.toBe(firstData);
    expect(dialogData().initialValue?.['productName']).toBe(
      component['localizedSupplyName'](supplyTwo.productName),
    );
    expect(component['addError']()).toBe('');
  });

  it('should create a new supply and reload the supply list', async () => {
    component['openAddDialog']();
    dialogClosed.next({ value: newSupply });
    await fixture.whenStable();
    await vi.waitFor(() => expect(createSupply).toHaveBeenCalledOnce());
    expect(createSupply).toHaveBeenCalledWith(inventoryId, {
      productName: newSupply.productName,
      supplierName: newSupply.supplierName,
      productDescription: newSupply.productDescription,
      productPrice: 10,
      productQuantity: 25,
      productSalePrice: 16,
      expirationDate: null,
    });
    expect(getSupplies).toHaveBeenCalledTimes(2);
    expect(component['addingSupply']()).toBe(false);
    expect(component['addError']()).toBe('');
  });

  it('should not open a supply form without management access', () => {
    roles.set([]);
    component['openAddDialog']();
    component['editSupply'](supplyOne);
    expect(openDialog).not.toHaveBeenCalled();
    expect(createSupply).not.toHaveBeenCalled();
    expect(updateSupply).not.toHaveBeenCalled();
  });

  it('should require positive numeric values in the modal fields', () => {
    component['openAddDialog']();
    for (const [key, min] of [
      ['productPrice', 0.01],
      ['productQuantity', 1],
      ['productSalePrice', 0.01],
    ] as const) {
      expect(dialogData().fields.find((field) => field.key === key)).toEqual(
        expect.objectContaining({ type: 'number', required: true, min }),
      );
    }
  });

  it('should update the selected supply instead of creating a new one', async () => {
    component['editSupply'](supplyOne);
    dialogClosed.next({ value: { ...newSupply, productName: 'Updated Bandage' } });
    await fixture.whenStable();
    await vi.waitFor(() => expect(updateSupply).toHaveBeenCalledOnce());
    expect(updateSupply).toHaveBeenCalledWith(inventoryId, supplyOne.productId, {
      productName: 'Updated Bandage',
      supplierName: newSupply.supplierName,
      productDescription: newSupply.productDescription,
      productPrice: 10,
      productQuantity: 25,
      productSalePrice: 16,
      expirationDate: null,
    });
    expect(createSupply).not.toHaveBeenCalled();
    expect(getSupplies).toHaveBeenCalledTimes(2);
    expect(component['addingSupply']()).toBe(false);
  });

  it('should show an error when creating a supply fails', async () => {
    createSupply.mockReturnValue(throwError(() => new Error('Create failed')));
    component['openAddDialog']();
    dialogClosed.next({ value: newSupply });
    await fixture.whenStable();
    await vi.waitFor(() =>
      expect(component['addError']()).toBe("Impossible d'ajouter l'article ou la photo."),
    );
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

    dialogClosed.next(undefined);
    dialogClosed.complete();

    expect(deleteSupply).not.toHaveBeenCalled();
  });

  it('should not delete after management access is removed while confirmation is open', () => {
    component['deleteSupply'](supplyOne);

    roles.set([]);
    dialogClosed.next({ value: {} });
    dialogClosed.complete();

    expect(deleteSupply).not.toHaveBeenCalled();
  });

  it('should delete a confirmed supply and reload the list', () => {
    component['deleteSupply'](supplyOne);

    expect(deleteSupply).not.toHaveBeenCalled();

    dialogClosed.next({ value: {} });
    dialogClosed.complete();

    expect(deleteSupply).toHaveBeenCalledWith(inventoryId, supplyOne.productId);

    expect(getSupplies).toHaveBeenCalledTimes(2);
  });

  it('should keep the table visible while refreshing after a deletion', () => {
    const table = fixture.nativeElement.querySelector('table');
    const refresh = new Subject<Supply[]>();
    getSupplies.mockReturnValue(refresh);

    component['deleteSupply'](supplyOne);
    dialogClosed.next({ value: {} });
    fixture.detectChanges();

    expect(getSupplies).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.querySelector('table')).toBe(table);
    refresh.complete();
  });

  it('should show an error when deleting a supply fails', () => {
    deleteSupply.mockReturnValue(throwError(() => new Error('Delete failed')));

    component['deleteSupply'](supplyOne);

    dialogClosed.next({ value: {} });
    dialogClosed.complete();

    expect(component['deleteError']()).toBe("Impossible de supprimer l'article.");
  });

  it('should not create a supply when the modal is cancelled', async () => {
    component['openAddDialog']();
    dialogClosed.next(undefined);
    await fixture.whenStable();
    expect(createSupply).not.toHaveBeenCalled();
    expect(updateSupply).not.toHaveBeenCalled();
  });

  it('should start a blank modal after cancelling an edit', async () => {
    component['editSupply'](supplyOne);
    component['addError'].set('Previous error');
    dialogClosed.next(undefined);
    await fixture.whenStable();
    component['openAddDialog']();
    expect(dialogData().initialValue).toEqual({
      productName: '',
      supplierName: '',
      productDescription: '',
      productPrice: 0,
      productQuantity: 1,
      productSalePrice: 0,
      expirationDate: '',
    });
    expect(dialogData().title).toBe('Ajouter un article');
    expect(component['addError']()).toBe('');
  });

  it('should format supply statuses for display', () => {
    expect(component['formatStatus']('OUT_OF_STOCK')).toBe('Rupture de stock');

    expect(component['formatStatus']('RE_ORDER')).toBe('À commander');
  });
});

describe('SupplyPage add modal validation', () => {
  it('rejects empty numeric fields and resets values and touched state after cancellation', async () => {
    const createSupply = vi.fn();
    await TestBed.configureTestingModule({
      imports: [SupplyPage],
      providers: [
        { provide: SupplyService, useValue: { getSupplies: () => of([]), createSupply } },
        { provide: AuthState, useValue: { roles: signal([Roles.admin]) } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ inventoryId: 'inventory' }) } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(SupplyPage);
    fixture.detectChanges();
    fixture.componentInstance['openAddDialog']();
    fixture.detectChanges();
    await fixture.whenStable();
    const modal = document.querySelector('mat-dialog-container')!;
    for (const [key, value] of Object.entries({
      productName: 'Gauze',
      supplierName: 'Supplier',
      productDescription: 'Pads',
      productPrice: '',
      productQuantity: '',
      productSalePrice: '',
    })) {
      const input = modal.querySelector<HTMLInputElement>(`#${key}`)!;
      input.value = value;
      input.dispatchEvent(new Event('input', { bubbles: true }));
      input.dispatchEvent(new Event('blur'));
    }
    fixture.detectChanges();
    const form = modal.querySelector('form')!;
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    const errors = Array.from(modal.querySelectorAll('.field-error'), (node) =>
      node.textContent?.trim(),
    );
    expect(errors).toContain('Le champ Prix co\u00fbtant (CAD) est obligatoire.');
    expect(errors).toContain('Le champ Quantit\u00e9 est obligatoire.');
    expect(errors).toContain('Le champ Prix de vente (CAD) est obligatoire.');
    expect(modal.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBe(true);
    expect(createSupply).not.toHaveBeenCalled();

    modal.querySelector<HTMLButtonElement>('.dialog-cancel-button')!.click();
    await vi.waitFor(() => expect(document.querySelector('mat-dialog-container')).toBeNull());
    fixture.componentInstance['openAddDialog']();
    fixture.detectChanges();
    await fixture.whenStable();
    const freshModal = document.querySelector('mat-dialog-container')!;
    expect(freshModal.querySelector<HTMLInputElement>('#productName')!.value).toBe('');
    expect(freshModal.querySelector<HTMLInputElement>('#productQuantity')!.value).toBe('1');
    expect(freshModal.querySelectorAll('.field-error')).toHaveLength(0);
    expect(freshModal.querySelector('form')!.classList.contains('ng-untouched')).toBe(true);
    freshModal.querySelector<HTMLButtonElement>('.dialog-cancel-button')!.click();
    await fixture.whenStable();
    expect(createSupply).not.toHaveBeenCalled();
  });
});
