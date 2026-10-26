import { LOCALE_ID } from '@angular/core';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { type Observable, of, Subject, throwError } from 'rxjs';
import { type Mock, vi } from 'vitest';

import { BillRecord } from '@features/bill/models/bill.model';
import { Customer } from '@features/bill/models/customer.model';
import { BillService } from '@features/bill/services/bill-service';

import { CustomerBillLookup } from './customer-bill-lookup';

describe('Customer bill lookup', () => {
  const customers = [
    { customerId: 'customer-1', firstName: 'Jane', lastName: 'Smith' },
    { customerId: 'customer-2', firstName: 'Alex', lastName: 'Martin' },
  ];

  const sampleBill: BillRecord = {
    billId: 'BILL-123',
    customerId: 'customer-1',
    customerFirstName: 'Jane',
    customerLastName: 'Smith',
    vetId: 'vet-1',
    vetFirstName: 'Sam',
    vetLastName: 'Brown',
    visitType: 'CHECKUP',
    date: new Date(2026, 9, 1),
    dueDate: new Date(2026, 10, 1),
    amount: 100,
    totalAmount: 114.98,
    billStatus: 'UNPAID',
    archive: false,
  };

  type RenderResult = {
    fixture: ComponentFixture<CustomerBillLookup>;
    host: HTMLElement;
    getCustomers: Mock<() => Observable<Customer>>;
    getBillsByCustomerId: Mock<(customerId: string) => Observable<BillRecord>>;
    selectCustomer: (customerId: string) => void;
  };

  async function render(): Promise<RenderResult> {
    const getCustomers = vi.fn<() => Observable<Customer>>(() => of(...customers));

    const getBillsByCustomerId = vi.fn<(customerId: string) => Observable<BillRecord>>(() =>
      of(sampleBill),
    );

    await TestBed.configureTestingModule({
      imports: [CustomerBillLookup],
      providers: [
        { provide: LOCALE_ID, useValue: 'en' },
        {
          provide: BillService,
          useValue: { getCustomers, getBillsByCustomerId },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(CustomerBillLookup);
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;

    const selectCustomer = (customerId: string): void => {
      const select = host.querySelector<HTMLSelectElement>('#customer-bill-select');

      if (!select) {
        throw new Error('Customer selector not found');
      }

      select.value = customerId;
      select.dispatchEvent(new Event('change'));
      fixture.detectChanges();
    };

    return {
      fixture,
      host,
      getCustomers,
      getBillsByCustomerId,
      selectCustomer,
    };
  }

  it('loads existing customers without requesting bills before selection', async () => {
    const { host, getCustomers, getBillsByCustomerId } = await render();

    expect(getCustomers).toHaveBeenCalledOnce();
    expect(host.textContent).toContain('Jane Smith');
    expect(host.textContent).toContain('Alex Martin');
    expect(host.textContent).toContain('Select a customer to display their bills.');
    expect(getBillsByCustomerId).not.toHaveBeenCalled();
    expect(host.querySelector('table')).toBeNull();
  });

  it('requests the selected customer’s bills and displays a read-only table', async () => {
    const { host, getBillsByCustomerId, selectCustomer } = await render();

    selectCustomer('customer-1');

    expect(getBillsByCustomerId).toHaveBeenCalledExactlyOnceWith('customer-1');

    const table = host.querySelector('table');

    expect(table?.textContent).toContain('BILL-123');
    expect(table?.textContent).toContain('114.98');
    expect(table?.textContent).toContain('Unpaid');
    expect(table?.querySelector('button, input, select, textarea')).toBeNull();
  });

  it('clears the previous customer’s bills when another customer is selected', async () => {
    const { host, getBillsByCustomerId, selectCustomer } = await render();

    selectCustomer('customer-1');
    expect(host.textContent).toContain('BILL-123');

    getBillsByCustomerId.mockReturnValue(of());

    selectCustomer('customer-2');

    expect(getBillsByCustomerId).toHaveBeenLastCalledWith('customer-2');
    expect(host.textContent).not.toContain('BILL-123');
    expect(host.textContent).toContain('This customer has no bills.');
  });

  it('shows an empty-state message when the customer has no bills', async () => {
    const { host, getBillsByCustomerId, selectCustomer } = await render();

    getBillsByCustomerId.mockReturnValue(of());

    selectCustomer('customer-1');

    expect(host.textContent).toContain('This customer has no bills.');
    expect(host.querySelector('table')).toBeNull();
    expect(host.querySelector('[role="alert"]')).toBeNull();
  });

  it('shows an error when retrieving bills fails', async () => {
    const { host, getBillsByCustomerId, selectCustomer } = await render();

    getBillsByCustomerId.mockReturnValue(throwError(() => new Error('Request failed')));

    selectCustomer('customer-1');

    expect(host.querySelector('[role="alert"]')?.textContent).toContain(
      'Unable to load bills. Please try again.',
    );
    expect(host.textContent).not.toContain('This customer has no bills.');
    expect(host.querySelector('table')).toBeNull();
  });

  it('ignores an older request after the selected customer changes', async () => {
    const { fixture, host, getBillsByCustomerId, selectCustomer } = await render();
    const firstRequest = new Subject<BillRecord>();
    const secondRequest = new Subject<BillRecord>();

    getBillsByCustomerId.mockReturnValueOnce(firstRequest).mockReturnValueOnce(secondRequest);

    selectCustomer('customer-1');
    selectCustomer('customer-2');

    secondRequest.complete();
    fixture.detectChanges();

    firstRequest.next(sampleBill);
    firstRequest.complete();
    fixture.detectChanges();

    expect(host.textContent).not.toContain('BILL-123');
    expect(host.textContent).toContain('This customer has no bills.');
  });
});
