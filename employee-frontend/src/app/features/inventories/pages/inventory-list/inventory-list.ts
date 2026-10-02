import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { Subscription, finalize } from 'rxjs';

import { isApiError, ApiError } from '@core/models/api-error';
import { AuthState } from '@core/services/auth-state';
import {
  Inventory,
  InventoryFilters,
  InventoryType,
} from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';
import { Roles } from '@shared/models/roles';

@Component({
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  selector: 'app-inventory-list',
  styleUrls: ['../../inventories.css', './inventory-list.css'],
  templateUrl: './inventory-list.html',
})
export class InventoryList implements OnInit, OnDestroy {
  private readonly inventoryService = inject(InventoryService);
  private readonly auth = inject(AuthState);
  private readonly destroyRef = inject(DestroyRef);

  private inventorySubscription?: Subscription;
  private inventoryTypesSubscription?: Subscription;
  private readonly quantitySubscriptions = new Set<Subscription>();
  private filterTimer?: ReturnType<typeof setTimeout>;

  protected readonly inventories = signal<Inventory[]>([]);
  protected readonly inventoryTypes = signal<InventoryType[]>([]);
  protected readonly inventoryTypesLoading = signal(false);
  protected readonly inventoryTypesError = signal<string | null>(null);
  protected readonly quantities = signal<Record<string, number | null>>({});
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<ApiError | null>(null);
  protected readonly noResultsMessage = signal<string | null>(null);
  protected readonly savingFavorites = signal<Record<string, boolean>>({});
  protected readonly favoriteError = signal<string | null>(null);

  protected readonly filters = signal<InventoryFilters>({
    inventoryName: '',
    inventoryType: '',
    inventoryDescription: '',
    importantOnly: false,
  });

  protected readonly canManageFavorites = computed(
    () => this.auth.hasRole(Roles.admin) || this.auth.hasRole(Roles.inventoryManager),
  );

  protected readonly isSavingAnyFavorite = computed(() =>
    Object.values(this.savingFavorites()).some(Boolean),
  );

  ngOnInit(): void {
    this.loadInventories();
    this.loadInventoryTypes();
  }

  ngOnDestroy(): void {
    if (this.filterTimer) {
      clearTimeout(this.filterTimer);
    }

    this.inventorySubscription?.unsubscribe();
    this.inventoryTypesSubscription?.unsubscribe();

    this.quantitySubscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.quantitySubscriptions.clear();
  }

  protected quantityFor(inventoryId: string): number | null {
    return this.quantities()[inventoryId] ?? null;
  }

  protected setTextFilter(field: 'inventoryName' | 'inventoryDescription', event: Event): void {
    const target = event.target;

    if (!(target instanceof HTMLInputElement)) {
      return;
    }

    this.filters.update((current) => ({
      ...current,
      [field]: target.value,
    }));

    this.scheduleFilteredLoad();
  }

  protected setInventoryTypeFilter(event: Event): void {
    const target = event.target;

    if (!(target instanceof HTMLSelectElement)) {
      return;
    }

    this.filters.update((current) => ({
      ...current,
      inventoryType: target.value,
    }));

    this.scheduleFilteredLoad();
  }

  protected setImportantOnly(event: Event): void {
    const target = event.target;

    if (!(target instanceof HTMLInputElement) || this.isSavingAnyFavorite()) {
      return;
    }

    this.filters.update((current) => ({
      ...current,
      importantOnly: target.checked,
    }));

    this.scheduleFilteredLoad();
  }

  protected clearFilters(): void {
    this.filters.set({
      inventoryName: '',
      inventoryType: '',
      inventoryDescription: '',
      importantOnly: false,
    });

    this.scheduleFilteredLoad();
  }

  protected hasActiveFilters(): boolean {
    const filters = this.filters();

    return Boolean(
      filters.inventoryName.trim() ||
      filters.inventoryType.trim() ||
      filters.inventoryDescription.trim() ||
      filters.importantOnly,
    );
  }

  protected isSavingFavorite(inventoryId: string): boolean {
    return this.savingFavorites()[inventoryId] ?? false;
  }

  protected toggleFavorite(inventory: Inventory): void {
    const id = inventory.inventoryId;

    if (!this.canManageFavorites() || this.isLoading() || this.isSavingFavorite(id)) {
      return;
    }

    const nextValue = !inventory.important;
    this.favoriteError.set(null);

    this.savingFavorites.update((current) => ({
      ...current,
      [id]: true,
    }));

    this.inventoryService
      .updateImportantStatus(id, nextValue)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.savingFavorites.update((current) => ({
            ...current,
            [id]: false,
          }));
        }),
      )
      .subscribe({
        next: () => {
          this.inventories.update((current) => {
            if (this.filters().importantOnly && !nextValue) {
              return current.filter((item) => item.inventoryId !== id);
            }

            return current.map((item) =>
              item.inventoryId === id ? { ...item, important: nextValue } : item,
            );
          });
        },
        // The star is only updated on success, so a failed save leaves it as it was.
        error: () => {
          this.favoriteError.set(`Could not update the favorite for ${inventory.inventoryName}.`);
        },
      });
  }

  private loadInventoryTypes(): void {
    this.inventoryTypesLoading.set(true);
    this.inventoryTypesError.set(null);

    this.inventoryTypesSubscription = this.inventoryService.getInventoryTypes().subscribe({
      next: (type) => {
        this.inventoryTypes.update((current) => {
          const alreadyLoaded = current.some((item) => item.typeId === type.typeId);
          return alreadyLoaded ? current : [...current, type];
        });
      },
      error: () => {
        this.inventoryTypesLoading.set(false);
        this.inventoryTypesError.set('Could not load inventory types. Try refreshing the page.');
      },
      complete: () => {
        this.inventoryTypesLoading.set(false);

        if (this.inventoryTypes().length === 0) {
          this.inventoryTypesError.set('No inventory types are available.');
        }
      },
    });
  }

  private scheduleFilteredLoad(): void {
    if (this.filterTimer) {
      clearTimeout(this.filterTimer);
    }

    this.inventorySubscription?.unsubscribe();

    this.quantitySubscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.quantitySubscriptions.clear();

    this.inventories.set([]);
    this.quantities.set({});
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.noResultsMessage.set(null);

    this.filterTimer = setTimeout(() => {
      this.filterTimer = undefined;
      this.loadInventories(this.filters());
    }, 300);
  }

  private loadInventories(filters: Partial<InventoryFilters> = this.filters()): void {
    this.inventorySubscription?.unsubscribe();

    this.quantitySubscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.quantitySubscriptions.clear();

    this.inventories.set([]);
    this.quantities.set({});
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.noResultsMessage.set(null);
    this.favoriteError.set(null);

    this.inventorySubscription = this.inventoryService.getInventories(filters).subscribe({
      next: (item) => {
        this.isLoading.set(false);
        this.errorMessage.set(null);
        this.noResultsMessage.set(null);

        this.inventories.update((current) => {
          const index = current.findIndex(
            (inventory) => inventory.inventoryId === item.inventoryId,
          );

          if (index === -1) {
            return [...current, item];
          }

          const updated = [...current];
          updated[index] = item;
          return updated;
        });

        this.loadQuantities(item.inventoryId);
      },
      error: (err: unknown) => {
        this.isLoading.set(false);

        if (err instanceof HttpErrorResponse && err.status === 404 && this.hasActiveFilters()) {
          this.noResultsMessage.set(
            'No inventory matched your search. Try a different name or description, or clear the filters.',
          );
          this.errorMessage.set(null);
          return;
        }

        this.noResultsMessage.set(null);
        this.errorMessage.set(
          isApiError(err) ? err : { code: 'UNKNOWN', message: 'Failed to load inventories.' },
        );
      },
      complete: () => {
        this.isLoading.set(false);

        if (this.inventories().length === 0 && this.hasActiveFilters()) {
          this.noResultsMessage.set(
            'No inventory matched your search. Try a different name or description, or clear the filters.',
          );
        }
      },
    });
  }

  private loadQuantities(inventoryId: string): void {
    if (!(inventoryId in this.quantities())) {
      this.quantities.update((current) => ({
        ...current,
        [inventoryId]: null,
      }));
    }

    let subscription = Subscription.EMPTY;
    const forgetSubscription = (): void => {
      this.quantitySubscriptions.delete(subscription);
    };

    subscription = this.inventoryService
      .getQuantity(inventoryId)
      .pipe(finalize(forgetSubscription))
      .subscribe({
        next: (quantity) => {
          this.quantities.update((current) => ({ ...current, [inventoryId]: quantity }));
        },
        error: () => {
          if (!(inventoryId in this.quantities())) {
            this.quantities.update((current) => ({
              ...current,
              [inventoryId]: null,
            }));
          }
        },
      });

    // Only track the request if it is still open.
    if (subscription.closed) {
      forgetSubscription();
    } else {
      this.quantitySubscriptions.add(subscription);
    }
  }
}
