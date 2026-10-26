import { HttpErrorResponse } from '@angular/common/http';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { type Observable, of, throwError } from 'rxjs';
import { type Mock, vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { BillDetails } from '@features/bill/components/bill-details/bill-details';
import { BillRecord } from '@features/bill/models/bill.model';
import { BillService } from '@features/bill/services/bill-service';
import { Roles } from '@shared/models/roles';

import { Bill } from './bill';

describe('Bill page', () => {
  const sampleBill: BillRecord = {
    billId: 'BILL-123',
    customerId: 'customer-1',
    customerFirstName: 'Jane',
    customerLastName: 'Smith',
    vetId: 'vet-1',
    vetFirstName: 'Alex',
    vetLastName: 'Martin',
    visitType: 'CHECKUP',
    date: new Date(2026, 9, 1),
    dueDate: new Date(2026, 10, 15),
    amount: 100,
    totalAmount: 114.98,
    billStatus: 'UNPAID',
    archive: false,
    interest: 0,
    taxedAmount: 14.98,
  };

  type RenderResult = {
    fixture: ComponentFixture<Bill>;
    host: HTMLElement;
    getAllBills: Mock<() => Observable<BillRecord>>;
    getMyBills: Mock<() => Observable<BillRecord>>;
    getBillById: Mock<(billId: string) => Observable<BillRecord>>;
    openDialog: Mock;
    getReceptionistBills: Mock<() => Observable<BillRecord>>;
    payBill: Mock;
  };

  async function render(
    roles: string[] = [],
    bills: BillRecord[] = [sampleBill],
  ): Promise<RenderResult> {
    const getAllBills = vi.fn<() => Observable<BillRecord>>(() => of(...bills));

    const getMyBills = vi.fn<() => Observable<BillRecord>>(() => of(...bills));

    const getBillById = vi.fn<(billId: string) => Observable<BillRecord>>(() => of(sampleBill));

    const getReceptionistBills = vi.fn<() => Observable<BillRecord>>(() => of(...bills));

    const payBill = vi.fn();

    const openDialog = vi.fn();

    await TestBed.configureTestingModule({
      imports: [Bill],
      providers: [
        {
          provide: AuthState,
          useValue: {
            hasRole: (role: string) => roles.includes(role),
          },
        },
        {
          provide: BillService,
          useValue: {
            getAllBills,
            getMyBills,
            getBillById,
            getReceptionistBills,
            payBill,
            getCustomers: vi.fn(() => of()),
            getBillsByCustomerId: vi.fn(() => of()),
          },
        },
        {
          provide: MatDialog,
          useValue: {
            open: openDialog,
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Bill);
    fixture.detectChanges();

    return {
      fixture,
      host: fixture.nativeElement as HTMLElement,
      getAllBills,
      getMyBills,
      getBillById,
      getReceptionistBills,
      payBill,
      openDialog,
    };
  }

  it('allows administrators to view all bills', async () => {
    const { host, getAllBills, getMyBills } = await render([Roles.admin]);

    expect(getAllBills).toHaveBeenCalledOnce();
    expect(getMyBills).not.toHaveBeenCalled();

    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).toContain('Jane');
    expect(host.querySelector('app-customer-bill-lookup')).toBeNull();
    expect(host.textContent).toContain('Bill Overview');
  });

  it('allows veterinarians to view their associated bills', async () => {
    const { host, getAllBills, getMyBills } = await render([Roles.vet]);

    expect(getAllBills).not.toHaveBeenCalled();
    expect(getMyBills).toHaveBeenCalledOnce();

    expect(host.textContent).toContain('My Bills');
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).toContain('Jane');
    expect(host.querySelector('app-customer-bill-lookup')).toBeNull();
    expect(host.textContent).toContain('Create Bill');
  });

  it('does not show update or archive actions to veterinarians', async () => {
    const { host } = await render([Roles.vet]);

    expect(host.querySelector('.bill-table-button--outline')).not.toBeNull();

    expect(host.querySelector('.bill-table-button--primary')).toBeNull();
    expect(host.querySelector('.bill-archive-action')).toBeNull();
    expect(host.querySelector('.bill-view-actions')).toBeNull();
  });

  it('allows veterinarians to view archived bills associated with them', async () => {
    const archivedBill: BillRecord = {
      ...sampleBill,
      archive: true,
    };

    const { host, getMyBills } = await render([Roles.vet], [archivedBill]);

    expect(getMyBills).toHaveBeenCalledOnce();
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).toContain('Archived');
  });

  it('shows an empty state when a veterinarian has no bills', async () => {
    const { host, getMyBills } = await render([Roles.vet], []);

    expect(getMyBills).toHaveBeenCalledOnce();
    expect(host.textContent).toContain('My Bills');
    expect(host.textContent).toContain('No bills found');
    expect(host.querySelector('tbody')).toBeNull();
  });

  it('allows administrators to switch between active and archived bills', async () => {
    const archivedBill: BillRecord = {
      ...sampleBill,
      billId: 'BILL-456',
      archive: true,
    };

    const { fixture, host } = await render([Roles.admin], [sampleBill, archivedBill]);

    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).not.toContain('BILL-456');

    const archivedButton = host.querySelectorAll<HTMLButtonElement>('.bill-view-action')[1];

    expect(archivedButton).toBeDefined();

    if (!archivedButton) {
      throw new Error('Archived bills toggle button not found');
    }

    archivedButton.click();
    fixture.detectChanges();

    expect(host.querySelector('tbody')?.textContent).toContain('BILL-456');
    expect(host.querySelector('tbody')?.textContent).not.toContain('BILL-123');
  });

  it('allows receptionists to view bills and pay unpaid bills', async () => {
    const { host, getAllBills, getMyBills, getReceptionistBills } = await render([
      Roles.receptionist,
    ]);

    expect(getReceptionistBills).toHaveBeenCalledOnce();
    expect(getAllBills).not.toHaveBeenCalled();
    expect(getMyBills).not.toHaveBeenCalled();

    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('.bill-row-actions')?.textContent).toContain('Pay Bill');
    expect(host.querySelector('.bill-row-actions')?.textContent).not.toContain('Update');
    expect(host.querySelector('.bill-archive-action')).toBeNull();
    expect(host.querySelector('app-customer-bill-lookup')).not.toBeNull();
  });

  it('allows administrators to open bill details in a dialog', async () => {
    const { fixture, host, getBillById, openDialog } = await render([Roles.admin]);

    const button = host.querySelector<HTMLButtonElement>('.bill-table-button--outline');

    expect(button).not.toBeNull();

    button?.click();
    fixture.detectChanges();

    expect(getBillById).toHaveBeenCalledWith('BILL-123');

    expect(openDialog).toHaveBeenCalledOnce();
    expect(openDialog).toHaveBeenCalledWith(
      BillDetails,
      expect.objectContaining({
        data: sampleBill,
      }),
    );
  });

  it('allows veterinarians to open details from their bill list', async () => {
    const { fixture, host, getBillById, openDialog } = await render([Roles.vet]);

    const button = host.querySelector<HTMLButtonElement>('.bill-table-button--outline');

    expect(button).not.toBeNull();

    button?.click();
    fixture.detectChanges();

    expect(getBillById).toHaveBeenCalledWith('BILL-123');

    expect(openDialog).toHaveBeenCalledOnce();
    expect(openDialog).toHaveBeenCalledWith(
      BillDetails,
      expect.objectContaining({
        data: sampleBill,
      }),
    );
  });

  it('allows veterinarians to find a bill by ID and open its dialog', async () => {
    const { fixture, host, getBillById, openDialog } = await render([Roles.vet]);

    const input = host.querySelector<HTMLInputElement>('#bill-id-lookup');

    expect(input).not.toBeNull();

    if (!input) {
      throw new Error('Bill ID input not found');
    }

    input.value = 'BILL-123';
    input.dispatchEvent(new Event('input', { bubbles: true }));

    host
      .querySelector('form.bill-lookup')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

    fixture.detectChanges();

    expect(getBillById).toHaveBeenCalledWith('BILL-123');

    expect(openDialog).toHaveBeenCalledOnce();
    expect(openDialog).toHaveBeenCalledWith(
      BillDetails,
      expect.objectContaining({
        data: sampleBill,
      }),
    );
  });

  it('shows an error when a bill does not exist', async () => {
    const { fixture, host, getBillById, openDialog } = await render([Roles.admin]);

    getBillById.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));

    host.querySelector<HTMLButtonElement>('.bill-table-button--outline')?.click();
    fixture.detectChanges();

    expect(getBillById).toHaveBeenCalledWith('BILL-123');

    expect(host.querySelector('[role="alert"]')?.textContent).toContain('Bill not found');

    expect(openDialog).not.toHaveBeenCalled();
  });
});
