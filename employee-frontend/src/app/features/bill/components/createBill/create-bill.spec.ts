import { TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY, of } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { BillService } from '@features/bill/services/bill-service';

import { CreateBill } from './create-bill';

describe('CreateBill', () => {
  const createBill = vi.fn(() => of({}));

  beforeEach(async () => {
    createBill.mockClear();

    await TestBed.configureTestingModule({
      imports: [CreateBill],
      providers: [
        {
          provide: AuthState,
          useValue: {
            userId: () => 'vet-1',
          },
        },
        {
          provide: BillService,
          useValue: {
            getCustomers: () => EMPTY,
            getVetByVetId: () =>
              of({
                vetId: 'vet-1',
                firstName: 'Test',
                lastName: 'Vet',
              }),
            createBill,
          },
        },
        {
          provide: MatDialogRef,
          useValue: { close: vi.fn() },
        },
        {
          provide: MatSnackBar,
          useValue: { open: vi.fn() },
        },
      ],
    }).compileComponents();
  });

  it('should auto-fill prices based on visit type', () => {
    const component = TestBed.createComponent(CreateBill).componentInstance;

    const prices = [
      { type: 'Check-Up', price: 50 },
      { type: 'Vaccine', price: 30 },
      { type: 'Surgery', price: 500 },
      { type: 'Dental', price: 200 },
    ];

    for (const visit of prices) {
      component['onVisitTypeChange'](visit.type);

      expect(component['billRequest']().amount).toBe(visit.price);
    }
  });

  it('should recalculate taxes after a manual price adjustment', () => {
    const component = TestBed.createComponent(CreateBill).componentInstance;

    component['onVisitTypeChange']('Surgery');

    component['billRequest'].update((bill) => ({
      ...bill,
      amount: 125,
    }));

    expect(component['priceSummary']()).toEqual({
      subtotal: 125,
      gst: 6.25,
      qst: 12.47,
      total: 143.72,
    });
  });

  it('should submit the manually adjusted amount', () => {
    const fixture = TestBed.createComponent(CreateBill);
    fixture.detectChanges();

    const component = fixture.componentInstance;

    component['onVisitTypeChange']('Surgery');

    component['billRequest'].update((bill) => ({
      ...bill,
      customerId: 'customer-1',
      vetId: 'vet-1',
      amount: 125,
    }));

    fixture.detectChanges();
    component.onSubmit(new Event('submit', { cancelable: true }));

    expect(createBill).toHaveBeenCalledWith(
      expect.objectContaining({
        visitType: 'Surgery',
        amount: 125,
      }),
      false,
    );
  });
});
