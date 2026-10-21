import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Status } from '@features/supplies/models/status';
import { Supply } from '@features/supplies/models/supply';
import { SupplyHistoryDialog } from '@features/supplies/pages/supply-history-dialog';
import { SupplyService } from '@features/supplies/services/supply-service';
import { Roles } from '@shared/models/roles';

import { SupplyPage } from './supply-page';

describe('SupplyPage supply history', () => {
  let fixture: ComponentFixture<SupplyPage>;
  let component: SupplyPage;
  let roles: ReturnType<typeof signal<string[]>>;
  let openDialog: ReturnType<typeof vi.fn>;

  const inventoryId = 'inventory-1';
  const supply: Supply = {
    productId: 'product-1',
    inventoryId,
    productName: 'Bandages',
    productDescription: 'Elastic bandages',
    productPrice: 8,
    productQuantity: 10,
    productSalePrice: 12,
    status: Status.AVAILABLE,
    lastUpdatedAt: '2026-10-14T20:38:52.412Z',
  };

  beforeEach(async () => {
    roles = signal<string[]>([Roles.admin]);
    openDialog = vi.fn();

    await TestBed.configureTestingModule({
      imports: [SupplyPage],
      providers: [
        {
          provide: SupplyService,
          useValue: { getSupplies: vi.fn().mockReturnValue(of([supply])) },
        },
        { provide: AuthState, useValue: { roles } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ inventoryId }) },
          },
        },
        { provide: MatDialog, useValue: { open: openDialog } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SupplyPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('opens the history dialog with the selected supply', () => {
    component['openSupplyHistory'](supply);

    expect(openDialog).toHaveBeenCalledWith(
      SupplyHistoryDialog,
      expect.objectContaining({
        data: { inventoryId, productId: supply.productId, supplyName: supply.productName },
        ariaLabel: 'History for Bandages',
      }),
    );
  });

  it('does not open the history dialog for users without management access', () => {
    roles.set([Roles.vet]);

    component['openSupplyHistory'](supply);

    expect(openDialog).not.toHaveBeenCalled();
  });
});
