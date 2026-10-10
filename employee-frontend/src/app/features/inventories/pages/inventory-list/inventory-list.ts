import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { Subscription, finalize, firstValueFrom } from 'rxjs';

import { isApiError, ApiError } from '@core/models/api-error';
import { AuthState } from '@core/services/auth-state';
import {
  Inventory,
  InventoryFilters,
  InventoryRequest,
  InventoryType,
} from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import {
  ModalFormData,
  ModalFormField,
  ModalFormResult,
} from '@shared/components/modal-form/modal-form-models';
import { getInventoryPermissions } from '@shared/models/inventory-permissions';
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
  private readonly dialog = inject(MatDialog);

  private inventorySubscription?: Subscription;
  private inventoryTypesSubscription?: Subscription;
  private readonly quantitySubscriptions = new Set<Subscription>();
  private filterTimer?: ReturnType<typeof setTimeout>;

  protected readonly can = computed(() => getInventoryPermissions(this.auth.roles()));

  protected readonly canManageFavorites = computed(
    () => this.auth.hasRole(Roles.admin) || this.auth.hasRole(Roles.inventoryManager),
  );

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

  protected readonly isSavingAnyFavorite = computed(() =>
    Object.values(this.savingFavorites()).some(Boolean),
  );

  protected get inventoryTypesDropdown(): string[] {
    return this.inventoryTypes().map((t) => t.type);
  }

  protected readonly savingInventory = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly deleteError = signal<string | null>(null);

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

  protected quantityFor(inventoryId: string): number | null {
    return this.quantities()[inventoryId] ?? null;
  }

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

  protected openAddDialog(): void {
    if (!this.can().canCreateInventory) return;

    void this.openInventoryDialog();
  }

  protected editInventory(inventory: Inventory): void {
    if (!this.can().canUpdateInventory) return;

    void this.openInventoryDialog(inventory);
  }

  private inventoryFields(): readonly ModalFormField[] {
    return [
      { key: 'inventoryName', label: 'Name', type: 'string', required: true },
      {
        key: 'inventoryType',
        label: 'Type',
        type: 'enum',
        required: true,
        options: this.inventoryTypes().map((type) => ({ value: type.type, label: type.type })),
      },
      { key: 'inventoryDescription', label: 'Description', type: 'string', required: true },
    ];
  }

  private async openInventoryDialog(inventory?: Inventory): Promise<void> {
    this.formError.set(null);

    if (this.inventoryTypesLoading() || this.inventoryTypes().length === 0) {
      this.formError.set(
        this.inventoryTypesError() ?? 'Inventory types are still loading. Try again in a moment.',
      );
      return;
    }

    const dialogRef = this.dialog.open<
      ModalForm<InventoryRequest>,
      ModalFormData<InventoryRequest>,
      ModalFormResult<InventoryRequest>
    >(ModalForm, {
      data: {
        title: inventory ? 'Edit Inventory' : 'Add Inventory',
        submitLabel: inventory ? 'Save Changes' : 'Add Inventory',
        fields: this.inventoryFields(),
        initialValue: inventory
          ? {
              inventoryName: inventory.inventoryName,
              inventoryType: inventory.inventoryType,
              inventoryDescription: inventory.inventoryDescription,
            }
          : { inventoryName: '', inventoryType: '', inventoryDescription: '' },
      },
      ariaLabel: inventory ? 'Edit inventory' : 'Add inventory',
      width: '480px',
      maxWidth: '95vw',
    });

    // The dialog closes with undefined when the user cancels.
    const result = await firstValueFrom(dialogRef.afterClosed());

    const allowed = inventory ? this.can().canUpdateInventory : this.can().canCreateInventory;

    if (!result || !allowed) return;

    await this.saveInventory(result.value, inventory?.inventoryId ?? null);
  }

  private async saveInventory(body: InventoryRequest, editingId: string | null): Promise<void> {
    if (this.savingInventory()) return;

    this.savingInventory.set(true);
    this.formError.set(null);

    try {
      await firstValueFrom(
        editingId
          ? this.inventoryService.updateInventory(editingId, body)
          : this.inventoryService.createInventory(body),
      );

      this.loadInventories(this.currentPage());
    } catch {
      this.formError.set(editingId ? 'Unable to update inventory.' : 'Unable to create inventory.');
    } finally {
      this.savingInventory.set(false);
    }
  }

  protected deleteInventoryAction(inventory: Inventory): void {
    if (!this.can().canDeleteInventory) return;

    this.deleteError.set(null);

    const dialogRef = this.dialog.open<
      ModalForm<Record<string, never>>,
      ModalFormData<Record<string, never>>,
      ModalFormResult<Record<string, never>>
    >(ModalForm, {
      data: {
        title: 'Delete Inventory',
        message: `Are you sure you want to delete ${inventory.inventoryName}?`,
        fields: [],
        initialValue: {},
        submitLabel: 'Delete',
        destructive: true,
      },
      ariaLabel: 'Delete inventory',
      width: '440px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result || !this.can().canDeleteInventory) return;

      this.inventoryService.deleteInventory(inventory.inventoryId).subscribe({
        next: () => {
          this.inventories.update((current) =>
            current.filter((inv) => inv.inventoryId !== inventory.inventoryId),
          );
        },
        error: () => {
          this.deleteError.set('Unable to delete inventory.');
        },
      });
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
    if (!this.can().hasAnyAccess) {
      this.isLoading.set(false);
      return;
    }

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
