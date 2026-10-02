import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { filter, map, Observable, retry, throwError, timer } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import {
  Inventory,
  InventoryRequest,
  InventoryFilters,
  InventoryType,
} from '@features/inventories/models/inventory.model';

export class InventoryList {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);
  private readonly baseUrl = '/api/gateway/inventories';

  getInventoryTypes(): Observable<InventoryType> {
    return this.sse
      .stream(`${this.baseUrl}/types`, { keepAlive: false, responseType: 'event' }, {}, 'GET')
      .pipe(
        filter((event): event is MessageEvent => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as InventoryType),
      );
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
    this.cancelQuantityRequests();
  }


  getInventories(filters: Partial<InventoryFilters> = {}): Observable<Inventory> {
    let params = new HttpParams().set('page', '0').set('size', '10');

    if (filters.inventoryName?.trim()) {
      params = params.set('inventoryName', filters.inventoryName.trim());
    }

    if (filters.inventoryType?.trim()) {
      params = params.set('inventoryType', filters.inventoryType.trim());
    }

    if (filters.inventoryDescription?.trim()) {
      params = params.set('inventoryDescription', filters.inventoryDescription.trim());
    }

    if (filters.importantOnly) {
      params = params.set('importantOnly', 'true');
    }

    return this.sse
      .stream(this.baseUrl, { keepAlive: false, responseType: 'event' }, { params }, 'GET')
      .pipe(
        filter((event): event is MessageEvent => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Inventory),
        retry({
          count: 5,
          delay: (error: unknown) => {
            const err = error as { code?: number; status?: number };
            const isNetworkError =
              error instanceof HttpErrorResponse
                ? error.status === 0
                : err?.code === 0 || err?.status === 0;

            return isNetworkError ? timer(5000) : throwError(() => error);
          },
        }),
      );
  }

  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }

  createInventory(body: InventoryRequest): Observable<Inventory> {
    return this.http.post<Inventory>(this.baseUrl, body);
  }

  updateInventory(inventoryId: string, body: InventoryRequest): Observable<Inventory> {
    return this.http.put<Inventory>(`${this.baseUrl}/${inventoryId}`, body);
  }

  deleteInventory(inventoryId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${inventoryId}`);
  }

  // GETs a single inventory by ID (ADMIN/INVENTORY_MANAGER only, VET excluded)
  getInventoryById(inventoryId: string): Observable<Inventory> {
    return this.sse
      .stream(
        `${this.baseUrl}/${inventoryId}`,
        { keepAlive: false, responseType: 'event' },
        {},
        'GET',
      )
      .pipe(
        filter(
          (event): event is MessageEvent => event instanceof MessageEvent && event.type !== 'error',
        ),
        map((event) => JSON.parse(event.data) as Inventory),
        retry({ count: Infinity, delay: () => timer(5000) }),
      );
  }

  updateImportantStatus(inventoryId: string, important: boolean): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${inventoryId}/important`, { important });
  protected setTextFilter(field: 'inventoryName' | 'inventoryDescription', event: Event): void {
    const target = event.target;
    if (!(target instanceof HTMLInputElement)) return;

    this.filters.update((current) => ({ ...current, [field]: target.value }));
    this.scheduleFilteredLoad();
  }

  protected setInventoryTypeFilter(event: Event): void {
    const target = event.target;
    if (!(target instanceof HTMLSelectElement)) return;

    this.filters.update((current) => ({ ...current, inventoryType: target.value }));
    this.scheduleFilteredLoad();
  }

  protected setImportantOnly(event: Event): void {
    const target = event.target;
    if (!(target instanceof HTMLInputElement) || this.isSavingAnyFavorite()) return;

    this.filters.update((current) => ({ ...current, importantOnly: target.checked }));
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
    if (this.currentPage() === 0 || this.isLoading()) return;

    const page = this.currentPage() - 1;
    this.currentPage.set(page);
    this.loadInventories(page);
  }

  protected goToNextPage(): void {
    if (!this.canGoToNextPage() || this.isLoading()) return;

    const page = this.currentPage() + 1;
    this.currentPage.set(page);
    this.loadInventories(page);
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
    this.savingFavorites.update((current) => ({ ...current, [id]: true }));

    this.inventoryService
      .updateImportantStatus(id, nextValue)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.savingFavorites.update((current) => ({ ...current, [id]: false }));
        }),
      )
      .subscribe({
        next: () => {
          if (this.filters().importantOnly && !nextValue) {
            this.loadInventories();
            return;
          }

          this.inventories.update((current) =>
            current.map((item) =>
              item.inventoryId === id ? { ...item, important: nextValue } : item,
            ),
          );
        },
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
          if (current.some((item) => item.typeId === type.typeId)) return current;
          return [...current, type];
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
    if (this.filterTimer) clearTimeout(this.filterTimer);

    this.currentPage.set(0);
    this.inventorySubscription?.unsubscribe();
    this.cancelQuantityRequests();
    this.inventories.set([]);
    this.quantities.set({});
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.noResultsMessage.set(null);
    this.favoriteError.set(null);

    this.filterTimer = setTimeout(() => {
      this.filterTimer = undefined;
      this.loadInventories(0);
    }, 300);
  }

  private loadInventories(page = this.currentPage()): void {
    const requestedPage = page;

    this.inventorySubscription?.unsubscribe();
    this.cancelQuantityRequests();
    this.inventories.set([]);
    this.quantities.set({});
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.noResultsMessage.set(null);
    this.favoriteError.set(null);

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

            if (index === -1) return [...current, item];

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
      this.quantities.update((current) => ({ ...current, [inventoryId]: null }));
    }

    let subscription = Subscription.EMPTY;
    const removeSubscription = (): void => {
      this.quantitySubscriptions.delete(subscription);
    };

    subscription = this.inventoryService
      .getQuantity(inventoryId)
      .pipe(finalize(removeSubscription))
      .subscribe({
        next: (quantity) => {
          this.quantities.update((current) => ({ ...current, [inventoryId]: quantity }));
        },
        error: () => {
          if (!(inventoryId in this.quantities())) {
            this.quantities.update((current) => ({ ...current, [inventoryId]: null }));
          }
        },
      });

    if (subscription.closed) {
      removeSubscription();
    } else {
      this.quantitySubscriptions.add(subscription);
    }
  }

  private cancelQuantityRequests(): void {
    this.quantitySubscriptions.forEach((subscription) => subscription.unsubscribe());
    this.quantitySubscriptions.clear();
  }
}
