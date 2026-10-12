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
    date: '2026-10-01',
    dueDate: '2026-11-15',
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
    getBillById: Mock<(billId: string) => Observable<BillRecord>>;
    openDialog: Mock;
  };

  async function render(roles: string[] = []): Promise<RenderResult> {
    const getAllBills = vi.fn<() => Observable<BillRecord>>(() => of(sampleBill));

    const getBillById = vi.fn<(billId: string) => Observable<BillRecord>>(() => of(sampleBill));

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
            getBillById,
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
      getBillById,
      openDialog,
    };
  }

  it('allows administrators to view all bills', async () => {
    const { host, getAllBills } = await render([Roles.admin]);

    expect(getAllBills).toHaveBeenCalledOnce();
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).toContain('Jane');
  });

  it('does not retrieve all bills for veterinarians', async () => {
    const { host, getAllBills } = await render([Roles.vet]);

    expect(getAllBills).not.toHaveBeenCalled();
    expect(host.querySelector('tbody')).toBeNull();
    expect(host.textContent).toContain('Create Bill');
  });

  it('allows administrators to open bill details in a dialog', async () => {
    const { fixture, host, getBillById, openDialog } = await render([Roles.admin]);

    const button = host.querySelector<HTMLButtonElement>('tbody button');
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

    host.querySelector<HTMLButtonElement>('tbody button')?.click();
    fixture.detectChanges();

    expect(getBillById).toHaveBeenCalledWith('BILL-123');

    expect(host.querySelector('[role="alert"]')?.textContent).toContain('Bill not found');

    expect(openDialog).not.toHaveBeenCalled();
  });
});
