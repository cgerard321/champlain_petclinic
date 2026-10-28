import { HttpErrorResponse } from '@angular/common/http';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { clearTranslations, loadTranslations } from '@angular/localize';
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
    interestExempt: false,
    taxedAmount: 14.98,
  };

  const overdueBill: BillRecord = {
    ...sampleBill,
    billStatus: 'OVERDUE',
    dueDate: new Date(2024, 0, 1),
    interest: 20,
    totalAmount: 134.98,
    interestExempt: false,
  };

  type RenderResult = {
    fixture: ComponentFixture<Bill>;
    host: HTMLElement;
    getAllBills: Mock<() => Observable<BillRecord>>;
    getMyBills: Mock<() => Observable<BillRecord>>;
    getBillById: Mock<(billId: string) => Observable<BillRecord>>;
    setInterestExempt: Mock<(billId: string, exempt: boolean) => Observable<void>>;
    openDialog: Mock<(...args: unknown[]) => { afterClosed: () => Observable<unknown> }>;
    getReceptionistBills: Mock<() => Observable<BillRecord>>;
    payBill: Mock;
  };

  afterEach(() => {
    clearTranslations();
  });

  async function render(
    roles: string[] = [],
    bills: BillRecord[] = [sampleBill],
  ): Promise<RenderResult> {
    const getAllBills = vi.fn<() => Observable<BillRecord>>(() => of(...bills));

    const getMyBills = vi.fn<() => Observable<BillRecord>>(() => of(...bills));

    const getBillById = vi.fn<(billId: string) => Observable<BillRecord>>(() => of(sampleBill));

    const getReceptionistBills = vi.fn<() => Observable<BillRecord>>(() => of(...bills));

    const setInterestExempt = vi.fn<(billId: string, exempt: boolean) => Observable<void>>(() =>
      of(undefined),
    );

    const payBill = vi.fn();

    // Both the confirmation dialog and Bill Details use afterClosed().
    const openDialog = vi.fn<(...args: unknown[]) => { afterClosed: () => Observable<unknown> }>(
      () => ({ afterClosed: () => of(undefined) }),
    );

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
            setInterestExempt,
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
      setInterestExempt,
      getReceptionistBills,
      payBill,
      openDialog,
    };
  }

  function findInterestButton(host: HTMLElement): HTMLButtonElement | undefined {
    return Array.from(host.querySelectorAll<HTMLButtonElement>('.bill-row-actions button')).find(
      (button) => /(?:Exempt Interest|Remove Interest Exemption)/.test(button.textContent ?? ''),
    );
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

  it('shows customer and veterinarian suggestions after three characters', async () => {
    const secondBill: BillRecord = {
      ...sampleBill,
      billId: 'BILL-456',
      customerFirstName: 'Bob',
      customerLastName: 'Jones',
      vetFirstName: 'Clara',
      vetLastName: 'Brown',
    };

    const { fixture, host } = await render([Roles.admin], [sampleBill, secondBill]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input');

    expect(input).not.toBeNull();

    if (!input) {
      throw new Error('Smart bill search input not found');
    }

    input.value = 'ja';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    expect(host.querySelectorAll('[role="option"]')).toHaveLength(0);

    input.value = 'jan';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();

    expect(host.querySelectorAll('[role="option"]')).toHaveLength(1);
    expect(host.querySelector('.bill-search-suggestion')?.textContent).toContain('Jane Smith');

    input.value = 'aRt';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();

    expect(host.querySelectorAll('[role="option"]')).toHaveLength(1);
    expect(host.querySelector('.bill-search-suggestion')?.textContent).toContain('Alex Martin');
    expect(host.querySelector('.bill-search-suggestion')?.textContent).toContain('Vétérinaire');
  });

  it('renders the French smart-search text by default', async () => {
    const { host } = await render([Roles.admin]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input');

    expect(host.textContent).toContain('Rechercher par nom de client ou de vétérinaire');
    expect(input?.getAttribute('placeholder')).toBe('Saisir au moins 3 caractères');
    expect(host.textContent).toContain('Rechercher des factures');
  });

  it('renders the English smart-search text when English translations are loaded', async () => {
    loadTranslations({
      'bills.smartSearch.label': 'Search by customer or veterinarian name',
      'bills.smartSearch.placeholder': 'Type at least 3 characters',
      'bills.smartSearch.submit': 'Search Bills',
      'bills.smartSearch.clear': 'Clear',
      'bills.smartSearch.clearLabel': 'Clear bill search',
      'bills.smartSearch.customerSuggestion': 'Customer',
      'bills.smartSearch.veterinarianSuggestion': 'Veterinarian',
      'bills.smartSearch.noResultsTitle': 'No matching bills',
      'bills.smartSearch.noResultsMessage':
        'No bills match « {$INTERPOLATION} ». Try another customer or veterinarian name.',
    });

    const { fixture, host } = await render([Roles.admin]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input')!;

    expect(host.textContent).toContain('Search by customer or veterinarian name');
    expect(input.getAttribute('placeholder')).toBe('Type at least 3 characters');
    expect(host.textContent).toContain('Search Bills');

    input.value = 'jan';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    expect(host.querySelector('.bill-search-suggestion small')?.textContent).toContain('Customer');
    expect(host.querySelector('.bill-smart-search__clear')?.textContent).toContain('Clear');
    expect(host.querySelector('.bill-smart-search__clear')?.getAttribute('aria-label')).toBe(
      'Clear bill search',
    );

    input.value = 'aRt';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    expect(host.querySelector('.bill-search-suggestion small')?.textContent).toContain(
      'Veterinarian',
    );

    input.value = 'nobody';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    host
      .querySelector('form.bill-smart-search')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(host.textContent).toContain('No matching bills');
    expect(host.textContent).toContain(
      'No bills match « nobody ». Try another customer or veterinarian name.',
    );
  });

  it('filters the bill list when a suggestion is selected', async () => {
    const secondBill: BillRecord = {
      ...sampleBill,
      billId: 'BILL-456',
      customerFirstName: 'Bob',
      customerLastName: 'Jones',
      vetFirstName: 'Clara',
      vetLastName: 'Brown',
    };

    const { fixture, host } = await render([Roles.admin], [sampleBill, secondBill]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input');

    if (!input) {
      throw new Error('Smart bill search input not found');
    }

    input.value = 'jan';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    host.querySelector<HTMLButtonElement>('.bill-search-suggestion')?.click();
    fixture.detectChanges();

    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).not.toContain('BILL-456');
    expect(input.value).toBe('Jane Smith');
  });

  it('applies the entered text when the smart search form is submitted', async () => {
    const secondBill: BillRecord = {
      ...sampleBill,
      billId: 'BILL-456',
      customerFirstName: 'Bob',
      customerLastName: 'Jones',
      vetFirstName: 'Clara',
      vetLastName: 'Brown',
    };

    const { fixture, host } = await render([Roles.admin], [sampleBill, secondBill]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input');

    if (!input) {
      throw new Error('Smart bill search input not found');
    }

    input.value = 'LaR';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    host
      .querySelector('form.bill-smart-search')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();

    expect(host.querySelector('tbody')?.textContent).toContain('BILL-456');
    expect(host.querySelector('tbody')?.textContent).not.toContain('BILL-123');
  });

  it('clears the smart search and restores the full bill list', async () => {
    const secondBill: BillRecord = {
      ...sampleBill,
      billId: 'BILL-456',
      customerFirstName: 'Bob',
      customerLastName: 'Jones',
      vetFirstName: 'Clara',
      vetLastName: 'Brown',
    };

    const { fixture, host } = await render([Roles.admin], [sampleBill, secondBill]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input');

    if (!input) {
      throw new Error('Smart bill search input not found');
    }

    input.value = 'jan';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    host
      .querySelector('form.bill-smart-search')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();

    host.querySelector<HTMLButtonElement>('.bill-smart-search__clear')?.click();
    fixture.detectChanges();

    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-456');
    expect(input.value).toBe('');
  });

  it('shows a specific empty state when no bills match the smart search', async () => {
    const { fixture, host } = await render([Roles.admin]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input');

    if (!input) {
      throw new Error('Smart bill search input not found');
    }

    input.value = 'nobody';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    host
      .querySelector('form.bill-smart-search')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();

    expect(host.querySelector('tbody')).toBeNull();
    expect(host.textContent).toContain('Aucune facture correspondante');
    expect(host.textContent).toContain('nobody');
  });

  it.each(['', '   '])('restores the list when the input is cleared to %j', async (value) => {
    const otherBill = {
      ...sampleBill,
      billId: 'BILL-456',
      customerFirstName: 'Bob',
      customerLastName: 'Jones',
    };
    const { fixture, host } = await render([Roles.admin], [sampleBill, otherBill]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input')!;

    input.value = 'jan';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    host
      .querySelector('form.bill-smart-search')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(host.querySelectorAll('tbody tr')).toHaveLength(1);

    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();

    expect(host.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(host.querySelectorAll('[role="option"]')).toHaveLength(0);
  });

  it.each([
    { key: 'status' as const, value: 'UNPAID', other: { billStatus: 'PAID' as const } },
    { key: 'visitType' as const, value: 'CHECKUP', other: { visitType: 'SURGERY' } },
    { key: 'year' as const, value: '2026', other: { date: new Date(2025, 9, 1) } },
    { key: 'month' as const, value: '10', other: { date: new Date(2026, 8, 1) } },
  ])('preserves the $key filter when searching and clearing', async ({ key, value, other }) => {
    const otherBill = { ...sampleBill, ...other, billId: 'BILL-456' };
    const { fixture, host } = await render([Roles.admin], [sampleBill, otherBill]);

    fixture.componentInstance['updateFilter'](key, value);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input')!;
    input.value = 'sMi';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    host
      .querySelector('form.bill-smart-search')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();

    expect(host.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    host.querySelector<HTMLButtonElement>('.bill-smart-search__clear')?.click();
    fixture.detectChanges();
    expect(host.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
  });

  it('keeps search results and suggestions within the selected archive view', async () => {
    const archivedBill = {
      ...sampleBill,
      billId: 'BILL-456',
      customerFirstName: 'Janet',
      archive: true,
    };
    const { fixture, host } = await render([Roles.admin], [sampleBill, archivedBill]);
    const input = host.querySelector<HTMLInputElement>('#bill-smart-search-input')!;
    input.value = 'jan';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
    expect(host.querySelectorAll('[role="option"]')).toHaveLength(1);
    expect(host.querySelector('.bill-search-suggestion')?.textContent).toContain('Jane Smith');

    host
      .querySelector('form.bill-smart-search')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-123');
    expect(host.querySelector('tbody')?.textContent).not.toContain('BILL-456');

    host.querySelectorAll<HTMLButtonElement>('.bill-view-action')[1]?.click();
    input.dispatchEvent(new Event('focus'));
    fixture.detectChanges();
    expect(host.querySelector('tbody')?.textContent).toContain('BILL-456');
    expect(host.querySelector('tbody')?.textContent).not.toContain('BILL-123');
    expect(host.querySelectorAll('[role="option"]')).toHaveLength(1);
    expect(host.querySelector('.bill-search-suggestion')?.textContent).toContain('Janet Smith');
  });

  describe('CPC-2135: overdue interest exemption', () => {
    it('shows Exempt Interest only for administrators on overdue bills', async () => {
      const { host } = await render([Roles.admin], [overdueBill]);

      expect(findInterestButton(host)?.textContent).toContain('Exempt Interest');
    });

    it.each(['UNPAID', 'PAID'] as const)(
      'does not show the exemption action for %s bills',
      async (billStatus) => {
        const bill: BillRecord = { ...sampleBill, billStatus };
        const { host } = await render([Roles.admin], [bill]);

        expect(findInterestButton(host)).toBeUndefined();
      },
    );

    it.each([Roles.vet, Roles.receptionist])(
      'does not show the exemption action to %s',
      async (role) => {
        const { host } = await render([role], [overdueBill]);

        expect(findInterestButton(host)).toBeUndefined();
      },
    );

    it('does not make an update request until the administrator confirms', async () => {
      const { fixture, host, openDialog, setInterestExempt } = await render(
        [Roles.admin],
        [overdueBill],
      );

      const button = findInterestButton(host);
      expect(button).toBeDefined();

      openDialog.mockReturnValueOnce({ afterClosed: () => of(undefined) });
      button?.click();
      fixture.detectChanges();

      expect(openDialog).toHaveBeenCalledOnce();
      expect(setInterestExempt).not.toHaveBeenCalled();
    });

    it('does not change the exemption when confirmation is cancelled', async () => {
      const { fixture, host, openDialog, setInterestExempt } = await render(
        [Roles.admin],
        [overdueBill],
      );

      openDialog.mockReturnValueOnce({ afterClosed: () => of(false) });
      findInterestButton(host)?.click();
      fixture.detectChanges();

      expect(setInterestExempt).not.toHaveBeenCalled();
      expect(host.querySelector('tbody')?.textContent).toContain('134.98');
      expect(host.querySelector('tbody')?.textContent).not.toContain('(Exempt)');
    });

    it('enables interest exemption and refreshes the total immediately', async () => {
      const { fixture, host, openDialog, setInterestExempt, getBillById } = await render(
        [Roles.admin],
        [overdueBill],
      );

      const exemptBill: BillRecord = {
        ...overdueBill,
        interestExempt: true,
        interest: 0,
        totalAmount: 114.98,
      };

      openDialog.mockReturnValueOnce({ afterClosed: () => of(true) });
      getBillById.mockReturnValue(of(exemptBill));

      findInterestButton(host)?.click();
      fixture.detectChanges();

      expect(setInterestExempt).toHaveBeenCalledTimes(1);
      expect(setInterestExempt).toHaveBeenCalledWith('BILL-123', true);
      expect(getBillById).toHaveBeenCalledWith('BILL-123');
      expect(host.querySelector('tbody')?.textContent).toContain('114.98');
      expect(host.querySelector('tbody')?.textContent).toContain('(Exempt)');
      expect(host.querySelector('tbody')?.textContent).toContain('OVERDUE');
      expect(host.querySelector('[role="status"]')?.textContent).toContain(
        'Interest exemption applied.',
      );
      expect(findInterestButton(host)?.textContent).toContain('Remove Interest Exemption');
    });

    it('removes the exemption and refreshes interest and total', async () => {
      const exemptBill: BillRecord = {
        ...overdueBill,
        interestExempt: true,
        interest: 0,
        totalAmount: 114.98,
      };

      const { fixture, host, openDialog, setInterestExempt, getBillById } = await render(
        [Roles.admin],
        [exemptBill],
      );

      openDialog.mockReturnValueOnce({ afterClosed: () => of(true) });
      getBillById.mockReturnValue(of(overdueBill));

      findInterestButton(host)?.click();
      fixture.detectChanges();

      expect(setInterestExempt).toHaveBeenCalledTimes(1);
      expect(setInterestExempt).toHaveBeenCalledWith('BILL-123', false);
      expect(host.querySelector('tbody')?.textContent).toContain('134.98');
      expect(host.querySelector('tbody')?.textContent).not.toContain('(Exempt)');
      expect(host.querySelector('tbody')?.textContent).toContain('OVERDUE');
      expect(host.querySelector('[role="status"]')?.textContent).toContain(
        'Interest exemption removed.',
      );
      expect(findInterestButton(host)?.textContent).toContain('Exempt Interest');
    });

    it('shows an error and keeps the original bill if the update fails', async () => {
      const { fixture, host, openDialog, setInterestExempt, getBillById } = await render(
        [Roles.admin],
        [overdueBill],
      );

      openDialog.mockReturnValueOnce({ afterClosed: () => of(true) });
      setInterestExempt.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

      findInterestButton(host)?.click();
      fixture.detectChanges();

      expect(setInterestExempt).toHaveBeenCalledTimes(1);
      expect(setInterestExempt).toHaveBeenCalledWith('BILL-123', true);
      expect(getBillById).not.toHaveBeenCalled();
      expect(host.querySelector('[role="alert"]')?.textContent).toContain(
        'Unable to update interest exemption',
      );
      expect(host.querySelector('tbody')?.textContent).toContain('134.98');
      expect(host.querySelector('tbody')?.textContent).not.toContain('(Exempt)');
    });

    it('explains when the exemption saves but refreshing the bill fails', async () => {
      const { fixture, host, openDialog, setInterestExempt, getBillById } = await render(
        [Roles.admin],
        [overdueBill],
      );

      openDialog.mockReturnValueOnce({ afterClosed: () => of(true) });
      getBillById.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 503 })));

      findInterestButton(host)?.click();
      fixture.detectChanges();

      expect(setInterestExempt).toHaveBeenCalledTimes(1);
      expect(setInterestExempt).toHaveBeenCalledWith('BILL-123', true);
      expect(getBillById).toHaveBeenCalledWith('BILL-123');
      expect(host.querySelector('[role="alert"]')?.textContent).toContain(
        'The exemption was saved, but the bill could not be refreshed',
      );
    });

    it('updates the table when Bill Details closes with a refreshed bill', async () => {
      const { fixture, host, openDialog, getBillById } = await render([Roles.admin], [overdueBill]);

      const exemptBill: BillRecord = {
        ...overdueBill,
        interestExempt: true,
        interest: 0,
        totalAmount: 114.98,
      };

      getBillById.mockReturnValue(of(overdueBill));
      openDialog.mockReturnValueOnce({ afterClosed: () => of(exemptBill) });

      const detailsButton = Array.from(
        host.querySelectorAll<HTMLButtonElement>('.bill-row-actions button'),
      ).find((button) => button.textContent?.trim() === 'Details');

      expect(detailsButton).toBeDefined();
      detailsButton?.click();
      fixture.detectChanges();

      expect(openDialog).toHaveBeenCalledWith(
        BillDetails,
        expect.objectContaining({ data: overdueBill }),
      );
      expect(host.querySelector('tbody')?.textContent).toContain('114.98');
      expect(host.querySelector('tbody')?.textContent).toContain('(Exempt)');
      expect(findInterestButton(host)?.textContent).toContain('Remove Interest Exemption');
    });
  });
});
