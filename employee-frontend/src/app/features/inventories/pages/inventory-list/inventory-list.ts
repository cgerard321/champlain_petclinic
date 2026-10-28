<<<<<<< Updated upstream
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, OnDestroy, OnInit, inject, signal } from '@angular/core';
=======
import {
  Component,
  computed,
  DestroyRef,
  LOCALE_ID,
  OnDestroy,
  OnInit,
  inject,
  signal,
} from '@angular/core';
>>>>>>> Stashed changes
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
<<<<<<< Updated upstream
import { Subscription, finalize } from 'rxjs';
=======
import {
  EMPTY,
  from,
  Observable,
  Subscription,
  expand,
  finalize,
  reduce,
  catchError,
  map,
  mergeMap,
  of,
  toArray,
  firstValueFrom,
} from 'rxjs';
>>>>>>> Stashed changes

import { isApiError, ApiError } from '@core/models/api-error';
import { AuthState } from '@core/services/auth-state';
import {
  Inventory,
  InventoryFilters,
  InventoryType,
} from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';
import { Roles } from '@shared/models/roles';

<<<<<<< Updated upstream
=======
const INVENTORY_SORTS = ['name-asc', 'name-desc', 'type', 'count-desc'] as const;
type InventorySort = (typeof INVENTORY_SORTS)[number];
const SORT_STORAGE_KEY = 'inventorySort';

interface InventoryFormValue {
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
}

>>>>>>> Stashed changes
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
  private quantitySubscription?: Subscription;

  protected readonly currentPage = signal(0);
  protected readonly pageSize = 8;
  protected readonly sourceInventories = signal<Inventory[]>([]);
  protected readonly sort = signal<InventorySort>(this.restoreSort());
  protected readonly quantityErrors = signal<Record<string, boolean>>({});
  protected readonly filteredInventories = computed(() =>
    this.sourceInventories().filter((inventory) =>
      this.matchesLocalizedFilters(inventory, this.filters()),
    ),
  );
  protected readonly countSortBlocked = computed(
    () =>
      this.sort() === 'count-desc' &&
      this.filteredInventories().some(
        (inventory) => this.quantityFor(inventory.inventoryId) === null,
      ),
  );
  protected readonly countSortError = computed(
    () =>
      this.countSortBlocked() &&
      this.filteredInventories().some((inventory) => this.quantityErrors()[inventory.inventoryId]),
  );
  protected readonly sortedInventories = computed(() => {
    // Never expose a partially ordered count result as the complete result.
    if (this.countSortBlocked()) return [];
    const compareName = (a: Inventory, b: Inventory): number =>
      this.localizedInventoryName(a.inventoryName).localeCompare(
        this.localizedInventoryName(b.inventoryName),
        this.locale,
      );
    return [...this.filteredInventories()].sort((a, b) => {
      let comparison = 0;
      switch (this.sort()) {
        case 'name-asc':
          comparison = compareName(a, b);
          break;
        case 'name-desc':
          comparison = compareName(b, a);
          break;
        case 'type':
          comparison = this.localizedInventoryType(a.inventoryType).localeCompare(
            this.localizedInventoryType(b.inventoryType),
            this.locale,
          );
          break;
        case 'count-desc':
          comparison = this.quantities()[b.inventoryId]! - this.quantities()[a.inventoryId]!;
          break;
      }
      return comparison || compareName(a, b) || a.inventoryId.localeCompare(b.inventoryId);
    });
  });
  protected readonly inventories = computed(() =>
    this.sortedInventories().slice(
      this.currentPage() * this.pageSize,
      (this.currentPage() + 1) * this.pageSize,
    ),
  );
  protected readonly inventoryTypes = signal<InventoryType[]>([]);
  protected readonly inventoryTypesLoading = signal(false);
  protected readonly inventoryTypesError = signal<string | null>(null);
  protected readonly quantities = signal<Record<string, number | null>>({});
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<ApiError | null>(null);
  protected readonly noResultsMessage = computed(() =>
    !this.isLoading() && this.filteredInventories().length === 0 && this.hasActiveFilters()
      ? this.emptyFilteredMessage()
      : null,
  );
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
    this.inventorySubscription?.unsubscribe();
    this.inventoryTypesSubscription?.unsubscribe();
    this.quantitySubscription?.unsubscribe();
  }

  protected quantityFor(inventoryId: string): number | null {
    return this.quantities()[inventoryId] ?? null;
  }

<<<<<<< Updated upstream
=======
  protected localizedInventoryName(name: string): string {
    return this.starterInventoryNames[name] ?? name;
  }

  protected localizedInventoryType(type: string): string {
    return this.starterInventoryTypes[type] ?? type;
  }

  protected localizedInventoryDescription(description: string): string {
    return this.starterInventoryDescriptions[description] ?? description;
  }

  private normalizeSearchText(value: string): string {
    return value
      .trim()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLocaleLowerCase(this.locale);
  }

  private matchesLocalizedFilters(inventory: Inventory, filters: InventoryFilters): boolean {
    const nameQuery = this.normalizeSearchText(filters.inventoryName);
    const descriptionQuery = this.normalizeSearchText(filters.inventoryDescription);
    const name = this.normalizeSearchText(this.localizedInventoryName(inventory.inventoryName));
    const description = this.normalizeSearchText(
      this.localizedInventoryDescription(inventory.inventoryDescription),
    );

    return (
      (!nameQuery ||
        name.startsWith(nameQuery) ||
        this.normalizeSearchText(inventory.inventoryName).startsWith(nameQuery)) &&
      (!descriptionQuery ||
        description.startsWith(descriptionQuery) ||
        this.normalizeSearchText(inventory.inventoryDescription).startsWith(descriptionQuery)) &&
      (!filters.inventoryType || inventory.inventoryType === filters.inventoryType) &&
      (!filters.importantOnly || !!inventory.important)
    );
  }

  private restoreSort(): InventorySort {
    const saved = sessionStorage.getItem(SORT_STORAGE_KEY);
    return INVENTORY_SORTS.find((sort) => sort === saved) ?? 'name-asc';
  }

  protected setSort(event: Event): void {
    const target = event.target;
    if (!(target instanceof HTMLSelectElement)) return;
    const sort = INVENTORY_SORTS.find((value) => value === target.value);
    if (!sort) return;
    this.sort.set(sort);
    sessionStorage.setItem(SORT_STORAGE_KEY, sort);
    this.currentPage.set(0);
  }

  private inventoryResults(): Observable<Inventory[]> {
    // Exact multiples require an extra empty batch to detect the end.
    const batchSize = 50;
    return this.inventoryService.getInventories({}, 0, batchSize).pipe(
      toArray(),
      expand((batch, index) =>
        batch.length === batchSize
          ? this.inventoryService.getInventories({}, index + 1, batchSize).pipe(toArray())
          : EMPTY,
      ),
      reduce((all, batch) => all.concat(batch), [] as Inventory[]),
    );
  }

  protected stockQuantityLabel(inventoryId: string): string {
    const count = this.quantityFor(inventoryId) ?? 0;
    return count === 1
      ? $localize`:@@inventory.quantity.one:${count}:count: article en stock`
      : $localize`:@@inventory.quantity.other:${count}:count: articles en stock`;
  }

  protected favoriteAriaLabel(inventory: Inventory): string {
    const name = this.localizedInventoryName(inventory.inventoryName);
    return inventory.important
      ? $localize`:@@inventory.favorite.remove:Retirer ${name}:name: des favoris`
      : $localize`:@@inventory.favorite.add:Ajouter ${name}:name: aux favoris`;
  }

>>>>>>> Stashed changes
  protected setTextFilter(field: 'inventoryName' | 'inventoryDescription', event: Event): void {
    const target = event.target;
    if (!(target instanceof HTMLInputElement)) return;

    this.filters.update((current) => ({ ...current, [field]: target.value }));
    this.currentPage.set(0);
  }

  protected setInventoryTypeFilter(event: Event): void {
    const target = event.target;
    if (!(target instanceof HTMLSelectElement)) return;

    this.filters.update((current) => ({ ...current, inventoryType: target.value }));
    this.currentPage.set(0);
  }

  protected setImportantOnly(event: Event): void {
    const target = event.target;
    if (!(target instanceof HTMLInputElement) || this.isSavingAnyFavorite()) return;

    this.filters.update((current) => ({ ...current, importantOnly: target.checked }));
    this.currentPage.set(0);
  }

  protected clearFilters(): void {
    this.filters.set({
      inventoryName: '',
      inventoryType: '',
      inventoryDescription: '',
      importantOnly: false,
    });
    this.currentPage.set(0);
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
    return (
      !this.countSortBlocked() &&
      (this.currentPage() + 1) * this.pageSize < this.filteredInventories().length
    );
  }

  protected goToPreviousPage(): void {
    if (this.currentPage() === 0 || this.isLoading() || this.countSortBlocked()) return;

    const page = this.currentPage() - 1;
    this.currentPage.set(page);
  }

  protected goToNextPage(): void {
    if (!this.canGoToNextPage() || this.isLoading()) return;

    const page = this.currentPage() + 1;
    this.currentPage.set(page);
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
          this.sourceInventories.update((current) =>
            current.map((item) =>
              item.inventoryId === id ? { ...item, important: nextValue } : item,
            ),
          );
          this.clampPage();
        },
        error: () => {
          this.favoriteError.set(`Could not update the favorite for ${inventory.inventoryName}.`);
        },
      });
  }

<<<<<<< Updated upstream
=======
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
      {
        key: 'inventoryName',
        label: $localize`:@@inventory.fields.name:Nom de l'inventaire`,
        type: 'string',
        required: true,
        errorMessages: {
          required: $localize`:@@inventory.validation.nameRequired:Le nom de l'inventaire est requis.`,
        },
      },
      {
        key: 'inventoryType',
        label: $localize`:@@inventory.fields.type:Type d'inventaire`,
        type: 'enum',
        required: true,
        errorMessages: {
          required: $localize`:@@inventory.validation.typeRequired:Le type d'inventaire est requis.`,
        },
        options: this.inventoryTypes().map((type) => ({
          value: type.type,
          label: this.localizedInventoryType(type.type),
        })),
      },
      {
        key: 'inventoryDescription',
        label: $localize`:@@inventory.fields.description:Description`,
        type: 'string',
        required: true,
        errorMessages: {
          required: $localize`:@@inventory.validation.descriptionRequired:La description est requise.`,
        },
      },
    ];
  }

  private async openInventoryDialog(inventory?: Inventory): Promise<void> {
    this.formError.set(null);

    if (this.inventoryTypesLoading() || this.inventoryTypes().length === 0) {
      this.formError.set(
        this.inventoryTypesError() ??
          $localize`:@@inventory.errors.typesLoading:Les types d'inventaire sont encore en chargement. Réessayez dans un instant.`,
      );
      return;
    }

    const dialogRef = this.dialog.open<
      ModalForm<InventoryFormValue>,
      ModalFormData<InventoryFormValue>,
      ModalFormResult<InventoryFormValue>
    >(ModalForm, {
      data: {
        title: inventory
          ? $localize`:@@inventory.actions.editTitle:Modifier l'inventaire`
          : $localize`:@@inventory.actions.add:Ajouter un inventaire`,
        submitLabel: inventory
          ? $localize`:@@inventory.actions.saveChanges:Enregistrer les modifications`
          : $localize`:@@inventory.actions.add:Ajouter un inventaire`,
        cancelLabel: $localize`:@@inventory.actions.cancel:Annuler`,
        fields: this.inventoryFields(),
        initialValue: inventory
          ? {
              inventoryName: this.localizedInventoryName(inventory.inventoryName),
              inventoryType: inventory.inventoryType,
              inventoryDescription: this.localizedInventoryDescription(
                inventory.inventoryDescription,
              ),
            }
          : { inventoryName: '', inventoryType: '', inventoryDescription: '' },
      },
      ariaLabel: inventory
        ? $localize`:@@inventory.actions.editTitle:Modifier l'inventaire`
        : $localize`:@@inventory.actions.add:Ajouter un inventaire`,
      width: '480px',
      maxWidth: '95vw',
    });

    // The dialog closes with undefined when the user cancels.
    const result = await firstValueFrom(dialogRef.afterClosed());

    const allowed = inventory ? this.can().canUpdateInventory : this.can().canCreateInventory;

    if (!result || !allowed) return;

    await this.saveInventory(result.value, inventory ?? null);
  }

  private async saveInventory(
    value: InventoryFormValue,
    editingInventory: Inventory | null,
  ): Promise<void> {
    if (this.savingInventory()) return;

    const editingId = editingInventory?.inventoryId ?? null;
    this.savingInventory.set(true);
    this.formError.set(null);

    try {
      const body = this.toInventoryRequest(value, editingInventory);

      await firstValueFrom(
        editingId
          ? this.inventoryService.updateInventory(editingId, body)
          : this.inventoryService.createInventory(body),
      );

      this.loadInventories();
    } catch {
      this.formError.set(
        editingId
          ? $localize`:@@inventory.errors.update:Impossible de modifier l'inventaire.`
          : $localize`:@@inventory.errors.create:Impossible de créer l'inventaire.`,
      );
    } finally {
      this.savingInventory.set(false);
    }
  }

  private toInventoryRequest(
    value: InventoryFormValue,
    original: Inventory | null,
  ): InventoryRequest {
    return {
      inventoryName:
        original && value.inventoryName === this.localizedInventoryName(original.inventoryName)
          ? original.inventoryName
          : value.inventoryName,
      inventoryType: value.inventoryType,
      inventoryDescription:
        original &&
        value.inventoryDescription ===
          this.localizedInventoryDescription(original.inventoryDescription)
          ? original.inventoryDescription
          : value.inventoryDescription,
    };
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
        title: $localize`:@@inventory.actions.deleteTitle:Supprimer l'inventaire`,
        message: $localize`:@@inventory.actions.deleteConfirm:Voulez-vous vraiment supprimer ${this.localizedInventoryName(inventory.inventoryName)}:name: ?`,
        fields: [],
        initialValue: {},
        submitLabel: $localize`:@@inventory.actions.delete:Supprimer`,
        cancelLabel: $localize`:@@inventory.actions.cancel:Annuler`,
        destructive: true,
      },
      ariaLabel: $localize`:@@inventory.actions.deleteTitle:Supprimer l'inventaire`,
      width: '440px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result || !this.can().canDeleteInventory) return;

      this.inventoryService.deleteInventory(inventory.inventoryId).subscribe({
        next: () => {
          this.sourceInventories.update((current) =>
            current.filter((inv) => inv.inventoryId !== inventory.inventoryId),
          );
          this.clampPage();
        },
        error: () => {
          this.deleteError.set(
            $localize`:@@inventory.errors.delete:Impossible de supprimer l'inventaire.`,
          );
        },
      });
    });
  }

>>>>>>> Stashed changes
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

  private clampPage(): void {
    const lastPage = Math.max(0, Math.ceil(this.filteredInventories().length / this.pageSize) - 1);
    this.currentPage.update((page) => Math.min(page, lastPage));
  }

<<<<<<< Updated upstream
  private loadInventories(page = this.currentPage()): void {
    const requestedPage = page;

=======
  private loadInventories(): void {
    if (!this.can().hasAnyAccess) {
      this.isLoading.set(false);
      return;
    }
>>>>>>> Stashed changes
    this.inventorySubscription?.unsubscribe();
    this.quantitySubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.favoriteError.set(null);
<<<<<<< Updated upstream

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
=======
    this.quantities.set({});
    this.quantityErrors.set({});
    this.inventorySubscription = this.inventoryResults().subscribe({
      next: (inventories) => {
        this.sourceInventories.set([
          ...new Map(inventories.map((inventory) => [inventory.inventoryId, inventory])).values(),
        ]);
        this.clampPage();
        this.isLoading.set(false);
        this.retryQuantities();
      },
      error: (err: unknown) => {
        this.isLoading.set(false);
        this.errorMessage.set({
          code: isApiError(err) ? err.code : 'UNKNOWN',
          message: $localize`:@@inventory.errors.load:Impossible de charger les inventaires.`,
        });
      },
    });
>>>>>>> Stashed changes
  }

  protected retryQuantities(): void {
    this.quantitySubscription?.unsubscribe();
    this.quantityErrors.set({});
    const missing = this.sourceInventories().filter(
      (inventory) => this.quantityFor(inventory.inventoryId) === null,
    );
    this.quantitySubscription = from(missing)
      .pipe(
        mergeMap(
          (inventory) =>
            this.inventoryService.getQuantity(inventory.inventoryId).pipe(
              map((quantity) => {
                if (!Number.isSafeInteger(quantity) || quantity < 0) {
                  throw new Error('Invalid supply count');
                }
                return { id: inventory.inventoryId, quantity };
              }),
              catchError(() => {
                this.quantityErrors.update((errors) => ({
                  ...errors,
                  [inventory.inventoryId]: true,
                }));
                return of({ id: inventory.inventoryId, quantity: null });
              }),
            ),
          4,
        ),
      )
      .subscribe(({ id, quantity }) => {
        this.quantities.update((quantities) => ({ ...quantities, [id]: quantity }));
      });
  }
}
