import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthState } from '@core/services/auth-state';
import { Inventory } from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';

import { InventoryList } from './inventory-list';

describe('InventoryList', () => {
  let fixture: ComponentFixture<InventoryList>;
  let roles: string[];

  const mainInventory: Inventory = {
    inventoryId: 'inv-1',
    inventoryName: 'Main',
    inventoryType: 'Pharmacy',
    inventoryDescription: 'Main inventory',
    important: false,
  };

  const inventoryService = {
    getInventories: vi.fn(),
    getQuantity: vi.fn(),
    getInventoryTypes: vi.fn(),
    updateImportantStatus: vi.fn(),
  };

  const createComponent = (): void => {
    fixture = TestBed.createComponent(InventoryList);
    fixture.detectChanges();
  };

  const stars = (): HTMLButtonElement[] =>
    Array.from(fixture.nativeElement.querySelectorAll('.favorite-button'));

  const checkbox = (): HTMLInputElement =>
    fixture.nativeElement.querySelector('input[type="checkbox"]');

  const setFavoritesOnly = (checked: boolean): void => {
    const input = checkbox();
    input.checked = checked;
    input.dispatchEvent(new Event('change'));
    vi.advanceTimersByTime(300);
    fixture.detectChanges();
  };

  beforeEach(async () => {
    vi.resetAllMocks();
    vi.useFakeTimers();
    inventoryService.getInventoryTypes.mockReturnValue(EMPTY);
    roles = ['ADMIN'];

    inventoryService.getInventories.mockReturnValue(of(mainInventory));
    inventoryService.getQuantity.mockReturnValue(of(10));
    inventoryService.updateImportantStatus.mockReturnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [InventoryList],
      providers: [
        {
          provide: InventoryService,
          useValue: inventoryService,
        },
        {
          provide: AuthState,
          useValue: { hasRole: (role: string) => roles.includes(role) },
        },
        {
          provide: ActivatedRoute,
          useValue: {},
        },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    fixture?.destroy();
    vi.useRealTimers();
  });

  it('should create', () => {
    // Act
    createComponent();

    // Assert
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load inventories without the favorite filter on initialization', () => {
    // Act
    createComponent();

    // Assert
    expect(inventoryService.getInventories).toHaveBeenCalledWith(
      expect.objectContaining({ importantOnly: false }),
      0,
      8,
    );
    expect(fixture.nativeElement.textContent).toContain('Main');
    expect(fixture.nativeElement.textContent).toContain('10 items in stock');
  });

  it('should reload from the backend when the favorites filter is turned on', () => {
    // Act
    createComponent();
    setFavoritesOnly(true);

    // Assert
    expect(inventoryService.getInventories).toHaveBeenLastCalledWith(
      expect.objectContaining({ importantOnly: true }),
      0,
      8,
    );
  });

  it('should drop the previous inventory and quantity requests when the filter changes', () => {
    // Arrange
    const firstStream = new Subject<Inventory>();
    const quantityRequest = new Subject<number>();

    inventoryService.getInventories
      .mockReturnValueOnce(firstStream)
      .mockReturnValueOnce(new Subject<Inventory>());
    inventoryService.getQuantity.mockReturnValue(quantityRequest);

    // Act
    createComponent();
    firstStream.next(mainInventory);
    fixture.detectChanges();

    // Assert
    expect(firstStream.observed).toBe(true);
    expect(quantityRequest.observed).toBe(true);

    // Act
    setFavoritesOnly(true);

    // Assert
    expect(firstStream.observed).toBe(false);
    expect(quantityRequest.observed).toBe(false);
  });

  it('should show the filtered empty state when the filter returns nothing', () => {
    // Arrange
    inventoryService.getInventories.mockReturnValue(EMPTY);

    // Act
    createComponent();

    // Assert
    expect(fixture.nativeElement.textContent).toContain(
      'There are no inventories to display right now.',
    );

    // Act
    setFavoritesOnly(true);

    // Assert
    expect(fixture.nativeElement.textContent).toContain('No matching inventory');
  });

  it.each(['VET', 'ADMIN', 'INVENTORY_MANAGER'])(
    'should let %s mark an inventory as favorite and back again',
    (role) => {
      roles = [role];
      // Act
      createComponent();
      expect(stars()[0]?.disabled).toBe(false);
      expect(stars()[0]?.getAttribute('aria-label')).toBe('Add favorite: Main');
      stars()[0]?.click();
      fixture.detectChanges();

      // Assert
      expect(inventoryService.updateImportantStatus).toHaveBeenCalledWith('inv-1', true);
      expect(stars()[0]?.getAttribute('aria-pressed')).toBe('true');
      expect(stars()[0]?.getAttribute('aria-label')).toBe('Remove favorite: Main');
      expect(stars()[0]?.querySelector('mat-icon')?.textContent?.trim()).toBe('star');

      // Act
      stars()[0]?.click();
      fixture.detectChanges();

      // Assert
      expect(inventoryService.updateImportantStatus).toHaveBeenLastCalledWith('inv-1', false);
      expect(stars()[0]?.getAttribute('aria-pressed')).toBe('false');
      expect(stars()[0]?.querySelector('mat-icon')?.textContent?.trim()).toBe('star_border');
    },
  );

  it('should keep the star unchanged and report a local error when the save fails', () => {
    // Arrange
    inventoryService.updateImportantStatus.mockReturnValue(throwError(() => new Error('failed')));

    // Act
    createComponent();
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(stars()[0]?.getAttribute('aria-pressed')).toBe('false');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Could not update the favorite for Main.',
    );
  });

  it('should disable the star while its save is pending', () => {
    // Arrange
    inventoryService.updateImportantStatus.mockReturnValue(new Subject<void>());

    // Act
    createComponent();
    stars()[0]?.click();
    fixture.detectChanges();
    stars()[0]?.click();

    // Assert
    expect(stars()[0]?.disabled).toBe(true);
    expect(inventoryService.updateImportantStatus).toHaveBeenCalledTimes(1);
  });

  it('should block filter changes while a favorite save is pending', () => {
    // Arrange
    inventoryService.updateImportantStatus.mockReturnValue(new Subject<void>());

    // Act
    createComponent();
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(checkbox().disabled).toBe(true);

    // Act
    setFavoritesOnly(true);

    // Assert
    expect(inventoryService.getInventories).toHaveBeenCalledTimes(1);
  });

  it('should remove an inventory from the list when it is unfavorited in favorites-only mode', () => {
    // Arrange
    inventoryService.getInventories.mockReturnValue(of({ ...mainInventory, important: true }));

    // Act
    createComponent();
    setFavoritesOnly(true);
    inventoryService.getInventories.mockReturnValue(EMPTY);
    stars()[0]?.click();
    fixture.detectChanges();

    // Assert
    expect(inventoryService.updateImportantStatus).toHaveBeenCalledWith('inv-1', false);
    expect(stars()).toHaveLength(0);
    expect(fixture.nativeElement.textContent).toContain('No matching inventory');
  });

  it.each(['VET', 'ADMIN', 'INVENTORY_MANAGER'])(
    'should synchronize %s favorites with the favorites-only list',
    (role) => {
      roles = [role];
      let important = false;
      inventoryService.getInventories.mockImplementation((filters) =>
        filters.importantOnly && !important ? EMPTY : of({ ...mainInventory, important }),
      );
      inventoryService.updateImportantStatus.mockImplementation((_id, value) => {
        important = value;
        return of(undefined);
      });
      createComponent();
      stars()[0]?.click();
      fixture.detectChanges();
      expect(stars()[0]?.getAttribute('aria-pressed')).toBe('true');
      setFavoritesOnly(true);
      expect(stars()).toHaveLength(1);
      expect(inventoryService.getInventories).toHaveBeenLastCalledWith(
        expect.objectContaining({ importantOnly: true }),
        0,
        8,
      );
      stars()[0]?.click();
      fixture.detectChanges();
      expect(important).toBe(false);
      expect(stars()).toHaveLength(0);
      expect(fixture.nativeElement.textContent).toContain('No matching inventory');
    },
  );

  it.each([['RECEPTIONIST'], ['OWNER'], []])('should block favorite changes for %j', (...role) => {
    // Arrange
    roles = role;

    // Act
    createComponent();
    stars()[0]?.click();
    fixture.componentInstance['toggleFavorite'](mainInventory);

    // Assert
    expect(stars()[0]?.disabled).toBe(true);
    expect(inventoryService.updateImportantStatus).not.toHaveBeenCalled();
    expect(checkbox().disabled).toBe(false);
  });
});
