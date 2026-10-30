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
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
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

const INVENTORY_SORTS = ['name-asc', 'name-desc', 'type', 'count-desc'] as const;
type InventorySort = (typeof INVENTORY_SORTS)[number];
const SORT_STORAGE_KEY = 'inventorySort';

interface InventoryFormValue {
  inventoryNameEn: string;
  inventoryNameFr: string;
  inventoryType: string;
  inventoryDescriptionEn: string;
  inventoryDescriptionFr: string;
}

@Component({
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  selector: 'app-inventory-list',
  styleUrls: ['./inventories.css', './inventory-list.css'],
  templateUrl: './inventory-list.html',
})
export class InventoryList implements OnInit, OnDestroy {
  private readonly inventoryService = inject(InventoryService);
  private readonly auth = inject(AuthState);
  private readonly destroyRef = inject(DestroyRef);
  private readonly locale = inject(LOCALE_ID);
  private readonly dialog = inject(MatDialog);

  private readonly starterInventoryTypes: Record<string, string> = {
    Equipment: $localize`:@@inventory.data.types.equipment:Équipement`,
    Bandages: $localize`:@@inventory.data.types.bandages:Bandages`,
    Injections: $localize`:@@inventory.data.types.injections:Injections`,
    Medications: $localize`:@@inventory.data.types.medications:Médicaments`,
    'Diagnostic Kits': $localize`:@@inventory.data.types.diagnosticKits:Trousses de diagnostic`,
  };

  private inventorySubscription?: Subscription;
  private inventoryTypesSubscription?: Subscription;
  private quantitySubscription?: Subscription;

  protected readonly can = computed(() => getInventoryPermissions(this.auth.roles()));

  protected readonly currentPage = signal(0);
  protected readonly pageSize = 8;
  protected readonly sourceInventories = signal<Inventory[]>([]);
  protected readonly sort = signal<InventorySort>(this.restoreSort());
  protected readonly quantityErrors = signal<Record<string, boolean>>({});
  protected readonly quantities = signal<Record<string, number | null>>({});

  protected readonly filters = signal<InventoryFilters>({
    inventoryName: '',
    inventoryType: '',
    inventoryDescription: '',
    importantOnly: false,
  });

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
    if (this.countSortBlocked()) return [];

    const compareName = (a: Inventory, b: Inventory): number =>
      this.localizedInventoryName(a).localeCompare(this.localizedInventoryName(b), this.locale);

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
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<ApiError | null>(null);

  protected readonly noResultsMessage = computed(() =>
    !this.isLoading() && this.filteredInventories().length === 0 && this.hasActiveFilters()
      ? this.emptyFilteredMessage()
      : null,
  );

  protected readonly savingFavorites = signal<Record<string, boolean>>({});
  protected readonly favoriteError = signal<string | null>(null);
  protected readonly loadingTypesLabel = $localize`:@@inventory.filters.loadingTypes:Chargement des types…`;
  protected readonly allTypesLabel = $localize`:@@inventory.filters.allTypes:Tous les types`;
  protected readonly noFavoritesLabel = $localize`:@@inventory.empty.noFavorites:Aucun inventaire favori trouvé.`;
  protected readonly emptyLabel = $localize`:@@inventory.empty.message:Aucun inventaire à afficher pour le moment.`;
  protected readonly savingLabel = $localize`:@@inventory.actions.saving:Enregistrement…`;
  protected readonly addInventoryLabel = $localize`:@@inventory.actions.add:Ajouter un inventaire`;

  protected readonly canManageFavorites = computed(
    () =>
      this.auth.hasRole(Roles.admin) ||
      this.auth.hasRole(Roles.inventoryManager) ||
      this.auth.hasRole(Roles.vet),
  );

  protected readonly isSavingAnyFavorite = computed(() =>
    Object.values(this.savingFavorites()).some(Boolean),
  );

  protected get inventoryTypesDropdown(): string[] {
    return this.inventoryTypes().map((type) => type.type);
  }

  protected readonly savingInventory = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly deleteError = signal<string | null>(null);

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

  protected localizedInventoryName(inventory: Inventory): string {
    return (
      (this.locale.startsWith('fr') ? inventory.name?.fr : inventory.name?.en) ??
      inventory.inventoryName
    );
  }

  protected localizedInventoryType(type: string): string {
    return this.starterInventoryTypes[type] ?? type;
  }

  protected localizedInventoryDescription(inventory: Inventory): string {
    return (
      (this.locale.startsWith('fr') ? inventory.description?.fr : inventory.description?.en) ??
      inventory.inventoryDescription
    );
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

    return (
      (!nameQuery ||
        [inventory.name?.en, inventory.name?.fr, inventory.inventoryName].some((text) =>
          this.normalizeSearchText(text ?? '').startsWith(nameQuery),
        )) &&
      (!descriptionQuery ||
        [inventory.description?.en, inventory.description?.fr, inventory.inventoryDescription].some(
          (text) => this.normalizeSearchText(text ?? '').startsWith(descriptionQuery),
        )) &&
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
    const name = this.localizedInventoryName(inventory);

    return inventory.important
      ? $localize`:@@inventory.favorite.remove:Retirer ${name}:name: des favoris`
      : $localize`:@@inventory.favorite.add:Ajouter ${name}:name: aux favoris`;
  }

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

  private emptyFilteredMessage(): string {
    const filters = this.filters();

    if (
      filters.importantOnly &&
      !filters.inventoryName.trim() &&
      !filters.inventoryType &&
      !filters.inventoryDescription.trim()
    ) {
      return this.noFavoritesLabel;
    }

    return $localize`:@@inventory.noResults.message:Aucun inventaire ne correspond à votre recherche. Essayez un autre nom ou une autre description, ou effacez les filtres.`;
  }

  protected canGoToNextPage(): boolean {
    return (
      !this.countSortBlocked() &&
      (this.currentPage() + 1) * this.pageSize < this.filteredInventories().length
    );
  }

  protected goToPreviousPage(): void {
    if (this.currentPage() === 0 || this.isLoading() || this.countSortBlocked()) return;

    this.currentPage.update((page) => page - 1);
  }

  protected goToNextPage(): void {
    if (!this.canGoToNextPage() || this.isLoading()) return;

    this.currentPage.update((page) => page + 1);
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
          const name = this.localizedInventoryName(inventory);
          this.favoriteError.set(
            $localize`:@@inventory.errors.favorite:Impossible de modifier le favori pour ${name}:name:.`,
          );
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
      {
        key: 'inventoryNameEn',
        label: $localize`:@@inventory.fields.nameEn:Nom de l'inventaire (anglais)`,
        type: 'string',
        required: true,
        errorMessages: {
          required: $localize`:@@inventory.validation.nameRequired:Le nom de l'inventaire est requis.`,
        },
      },
      {
        key: 'inventoryNameFr',
        label: $localize`:@@inventory.fields.nameFr:Nom de l'inventaire (français)`,
        type: 'string',
        required: true,
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
        key: 'inventoryDescriptionEn',
        label: $localize`:@@inventory.fields.descriptionEn:Description (anglais)`,
        type: 'string',
        required: true,
        errorMessages: {
          required: $localize`:@@inventory.validation.descriptionRequired:La description est requise.`,
        },
      },
      {
        key: 'inventoryDescriptionFr',
        label: $localize`:@@inventory.fields.descriptionFr:Description (français)`,
        type: 'string',
        required: true,
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
              inventoryNameEn: inventory.name?.en ?? inventory.inventoryName,
              inventoryNameFr: inventory.name?.fr ?? inventory.inventoryName,
              inventoryType: inventory.inventoryType,
              inventoryDescriptionEn: inventory.description?.en ?? inventory.inventoryDescription,
              inventoryDescriptionFr: inventory.description?.fr ?? inventory.inventoryDescription,
            }
          : {
              inventoryNameEn: '',
              inventoryNameFr: '',
              inventoryType: '',
              inventoryDescriptionEn: '',
              inventoryDescriptionFr: '',
            },
      },
      ariaLabel: inventory
        ? $localize`:@@inventory.actions.editTitle:Modifier l'inventaire`
        : $localize`:@@inventory.actions.add:Ajouter un inventaire`,
      width: '480px',
      maxWidth: '95vw',
    });

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
      const body = this.toInventoryRequest(value);

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

  private toInventoryRequest(value: InventoryFormValue): InventoryRequest {
    return {
      inventoryName: value.inventoryNameEn.trim(),
      inventoryType: value.inventoryType,
      inventoryDescription: value.inventoryDescriptionEn.trim(),
      name: {
        en: value.inventoryNameEn.trim(),
        fr: value.inventoryNameFr.trim(),
      },
      description: {
        en: value.inventoryDescriptionEn.trim(),
        fr: value.inventoryDescriptionFr.trim(),
      },
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
        message: $localize`:@@inventory.actions.deleteConfirm:Voulez-vous vraiment supprimer ${this.localizedInventoryName(inventory)}:name: ?`,
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

    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result || !this.can().canDeleteInventory) return;

        this.inventoryService
          .deleteInventory(inventory.inventoryId)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.sourceInventories.update((current) =>
                current.filter((item) => item.inventoryId !== inventory.inventoryId),
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
        this.inventoryTypesError.set(
          $localize`:@@inventory.errors.types:Impossible de charger les types d'inventaire. Actualisez la page.`,
        );
      },
      complete: () => {
        this.inventoryTypesLoading.set(false);

        if (this.inventoryTypes().length === 0) {
          this.inventoryTypesError.set(
            $localize`:@@inventory.errors.noTypes:Aucun type d'inventaire disponible.`,
          );
        }
      },
    });
  }

  private clampPage(): void {
    const lastPage = Math.max(0, Math.ceil(this.filteredInventories().length / this.pageSize) - 1);
    this.currentPage.update((page) => Math.min(page, lastPage));
  }

  private loadInventories(): void {
    if (!this.can().hasAnyAccess) {
      this.isLoading.set(false);
      return;
    }

    this.inventorySubscription?.unsubscribe();
    this.quantitySubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.favoriteError.set(null);
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
