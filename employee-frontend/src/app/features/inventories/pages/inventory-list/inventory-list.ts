import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { isApiError, ApiError } from '@core/models/api-error';
import {
  Inventory,
  InventoryFilters,
  InventoryType,
} from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';

@Component({
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  selector: 'app-inventory-list',
  styleUrls: ['../../inventories.css', './inventory-list.css'],
  templateUrl: './inventory-list.html',
})
export class InventoryList implements OnInit, OnDestroy {
  private readonly inventoryService = inject(InventoryService);
  private inventorySubscription?: Subscription;
  private inventoryTypesSubscription?: Subscription;
  private readonly quantitySubscriptions = new Set<Subscription>();
  private filterTimer?: ReturnType<typeof setTimeout>;

  protected readonly currentPage = signal(0);
  protected readonly pageSize = 8;
  protected readonly inventories = signal<Inventory[]>([]);
  protected readonly inventoryTypes = signal<InventoryType[]>([]);
  protected readonly inventoryTypesLoading = signal(false);
  protected readonly inventoryTypesError = signal<string | null>(null);
  protected readonly quantities = signal<Record<string, number | null>>({});
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<ApiError | null>(null);
  protected readonly noResultsMessage = signal<string | null>(null);
  protected readonly filters = signal<InventoryFilters>({
    inventoryName: '',
    inventoryType: '',
    inventoryDescription: '',
    importantOnly: false,
  });

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

    if (!(target instanceof HTMLInputElement)) {
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

  protected canGoToNextPage(): boolean {
    return this.inventories().length === this.pageSize;
  }

  protected goToPreviousPage(): void {
    if (this.currentPage() === 0 || this.isLoading()) {
      return;
    }

    const page = this.currentPage() - 1;
    this.currentPage.set(page);
    this.loadInventories(page);
  }

  protected goToNextPage(): void {
    if (!this.canGoToNextPage() || this.isLoading()) {
      return;
    }

    const page = this.currentPage() + 1;
    this.currentPage.set(page);
    this.loadInventories(page);
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

    this.currentPage.set(0);
    this.inventorySubscription?.unsubscribe();
    this.inventories.set([]);
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.noResultsMessage.set(null);

    this.filterTimer = setTimeout(() => {
      this.filterTimer = undefined;
      this.loadInventories(0);
    }, 300);
  }

  private loadInventories(page = this.currentPage()): void {
    const requestedPage = page;

    this.inventorySubscription?.unsubscribe();
    this.inventories.set([]);
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.noResultsMessage.set(null);

    this.inventorySubscription = this.inventoryService
      .getInventories(this.filters(), requestedPage, this.pageSize)
      .subscribe({
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

          if (err instanceof HttpErrorResponse && err.status === 404 && requestedPage > 0) {
            const previousPage = requestedPage - 1;
            this.currentPage.set(previousPage);
            this.loadInventories(previousPage);
            return;
          }

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

          if (this.inventories().length === 0 && requestedPage > 0) {
            const previousPage = requestedPage - 1;
            this.currentPage.set(previousPage);
            this.loadInventories(previousPage);
            return;
          }

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

    const subscription = this.inventoryService.getQuantity(inventoryId).subscribe({
      next: (quantity) => {
        this.quantities.update((current) => ({
          ...current,
          [inventoryId]: quantity,
        }));
      },
      error: () => {
        if (!(inventoryId in this.quantities())) {
          this.quantities.update((current) => ({
            ...current,
            [inventoryId]: null,
          }));
        }
      },
      complete: () => {
        this.quantitySubscriptions.delete(subscription);
      },
    });

    this.quantitySubscriptions.add(subscription);
  }
}
