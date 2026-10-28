import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { SupplyAuditEntry } from '@features/supplies/models/supply';
import { SupplyService } from '@features/supplies/services/supply-service';

import { SupplyHistoryDialog, SupplyHistoryDialogData } from './supply-history-dialog';

describe('SupplyHistoryDialog', () => {
  let fixture: ComponentFixture<SupplyHistoryDialog>;
  let getSupplyHistory: ReturnType<typeof vi.fn<SupplyService['getSupplyHistory']>>;
  let close: ReturnType<typeof vi.fn>;

  const dialogData: SupplyHistoryDialogData = {
    inventoryId: 'inventory-1',
    productId: 'product-1',
    supplyName: 'Bandages',
  };

  const entry: SupplyAuditEntry = {
    id: 'audit-1',
    inventoryId: dialogData.inventoryId,
    supplyId: dialogData.productId,
    supplyName: dialogData.supplyName,
    previousQuantity: 8,
    newQuantity: 10,
    quantityChange: 2,
    action: 'QUANTITY_UPDATED',
    changedAt: '2026-10-14T20:38:52.412Z',
    changedBy: 'test-user',
  };

  beforeEach(async () => {
    getSupplyHistory = vi.fn<SupplyService['getSupplyHistory']>().mockReturnValue(of([entry]));
    close = vi.fn();

    await TestBed.configureTestingModule({
      imports: [SupplyHistoryDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: dialogData },
        { provide: MatDialogRef, useValue: { close } },
        { provide: SupplyService, useValue: { getSupplyHistory } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SupplyHistoryDialog);
    fixture.detectChanges();
  });

  it('loads and displays the supply history in the dialog', () => {
    expect(getSupplyHistory).toHaveBeenCalledWith(dialogData.inventoryId, dialogData.productId);

    const content = fixture.nativeElement.textContent as string;
    expect(content).toContain('Historique : Bandages');
    expect(content).toContain('Quantit\u00e9 modifi\u00e9e');
    expect(content).toContain('8 → 10 (+2)');
    expect(content).toContain('Par test-user');
    expect(content).toContain('2026');
  });

  it('shows an empty state when there are no audit entries', () => {
    getSupplyHistory.mockReturnValue(of([]));
    fixture = TestBed.createComponent(SupplyHistoryDialog);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Aucun historique disponible pour cet article.',
    );
  });

  it('shows an error state when history cannot be loaded', () => {
    getSupplyHistory.mockReturnValue(throwError(() => new Error('Request failed')));
    fixture = TestBed.createComponent(SupplyHistoryDialog);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      "Impossible de charger l'historique de l'article.",
    );
  });

  it('closes when the close button is selected', () => {
    fixture.nativeElement.querySelector('button').click();

    expect(close).toHaveBeenCalled();
  });
});
