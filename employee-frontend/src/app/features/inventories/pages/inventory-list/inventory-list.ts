import { HttpErrorResponse } from '@angular/common/http';
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
  switchMap,
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

// These values are the starter records created by the inventory service. Keep the API values
// unchanged so existing filters, links, and favorite updates continue to use the stored data.
const starterInventoryNames: Record<string, string> = {
  'Medical equipment': $localize`:@@inventory.data.names.medicalEquipment:Équipement médical`,
  'First-Aid': $localize`:@@inventory.data.names.firstAid:Premiers soins`,
  Vaccines: $localize`:@@inventory.data.names.vaccines:Vaccins`,
  Medications: $localize`:@@inventory.data.names.medications:Médicaments`,
  'Pet Carriers': $localize`:@@inventory.data.names.petCarriers:Cages de transport pour animaux`,
  'Diagnostic Kits': $localize`:@@inventory.data.names.diagnosticKits:Trousses de diagnostic`,
  'Surgical Instruments': $localize`:@@inventory.data.names.surgicalInstruments:Instruments chirurgicaux`,
  'Anesthesia Supplies': $localize`:@@inventory.data.names.anesthesiaSupplies:Fournitures d'anesthésie`,
  'Wound Care Supplies': $localize`:@@inventory.data.names.woundCareSupplies:Fournitures de soins des plaies`,
  'Infectious Disease Test Kits': $localize`:@@inventory.data.names.infectiousDiseaseTestKits:Trousses de dépistage des maladies infectieuses`,
};

const starterInventoryTypes: Record<string, string> = {
  Equipment: $localize`:@@inventory.data.types.equipment:Équipement`,
  Bandages: $localize`:@@inventory.data.types.bandages:Bandages`,
  Injections: $localize`:@@inventory.data.types.injections:Injections`,
  Medications: $localize`:@@inventory.data.types.medications:Médicaments`,
  'Diagnostic Kits': $localize`:@@inventory.data.types.diagnosticKits:Trousses de diagnostic`,
};

const starterInventoryDescriptions: Record<string, string> = {
  'Medical equipment for surgery': $localize`:@@inventory.data.descriptions.medicalEquipment:Équipement médical pour la chirurgie`,
  'First-aid supplies for pet emergencies': $localize`:@@inventory.data.descriptions.firstAid:Fournitures de premiers soins pour les urgences chez les animaux`,
  'Supplies for disease prevention': $localize`:@@inventory.data.descriptions.vaccines:Fournitures pour la prévention des maladies`,
  'Antibiotics for pet infections': $localize`:@@inventory.data.descriptions.medications:Antibiotiques pour traiter les infections chez les animaux`,
  'Carriers for transporting pets': $localize`:@@inventory.data.descriptions.petCarriers:Cages pour transporter les animaux`,
  'Kits for diagnosing various pet illnesses and conditions': $localize`:@@inventory.data.descriptions.diagnosticKits:Trousses pour diagnostiquer diverses maladies et affections chez les animaux`,
  'Tools for performing surgical procedures on pets': $localize`:@@inventory.data.descriptions.surgicalInstruments:Outils pour effectuer des interventions chirurgicales sur les animaux`,
  'Supplies for administering anesthesia during surgeries': $localize`:@@inventory.data.descriptions.anesthesiaSupplies:Fournitures pour administrer l'anesthésie pendant les interventions chirurgicales`,
  'Supplies for treating and dressing wounds': $localize`:@@inventory.data.descriptions.woundCareSupplies:Fournitures pour traiter et panser les plaies`,
  'Kits for testing infectious diseases in pets': $localize`:@@inventory.data.descriptions.infectiousDiseaseTestKits:Trousses pour dépister les maladies infectieuses chez les animaux`,
};
interface InventoryFormValue {
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
}

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
  private readonly locale = inject(LOCALE_ID);
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
  protected readonly loadingTypesLabel = $localize`:@@inventory.filters.loadingTypes:Chargement des types…`;
  protected readonly allTypesLabel = $localize`:@@inventory.filters.allTypes:Tous les types`;
  protected readonly noFavoritesLabel = $localize`:@@inventory.empty.noFavorites:Aucun inventaire favori trouvé.`;
  protected readonly emptyLabel = $localize`:@@inventory.empty.message:Aucun inventaire à afficher pour le moment.`;
  protected readonly savingLabel = $localize`:@@inventory.actions.saving:Enregistrement…`;
  protected readonly addInventoryLabel = $localize`:@@inventory.actions.add:Ajouter un inventaire`;

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

  protected localizedInventoryName(name: string): string {
    return starterInventoryNames[name] ?? name;
  }

  protected localizedInventoryType(type: string): string {
    return starterInventoryTypes[type] ?? type;
  }

  protected localizedInventoryDescription(description: string): string {
    return starterInventoryDescriptions[description] ?? description;
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

  private inventoryResults(page: number): Observable<Inventory> {
    const filters = this.filters();
    if (
      !filters.inventoryName.trim() &&
      !filters.inventoryDescription.trim() &&
      !filters.inventoryType &&
      !filters.importantOnly
    ) {
      return this.inventoryService.getInventories(filters, page, this.pageSize);
    }

    // Read the stored records and apply every active filter to the display values locally.
    // This keeps translated text searches, the type dropdown, and favorites consistent.
    const batchSize = 50;
    return this.inventoryService.getInventories({}, 0, batchSize).pipe(
      toArray(),
      expand((batch, index) =>
        batch.length === batchSize
          ? this.inventoryService.getInventories({}, index + 1, batchSize).pipe(toArray())
          : EMPTY,
      ),
      reduce((all, batch) => all.concat(batch), [] as Inventory[]),
      switchMap((all) =>
        from(
          all
            .filter((inventory) => this.matchesLocalizedFilters(inventory, filters))
            .slice(page * this.pageSize, (page + 1) * this.pageSize),
        ),
      ),
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
          const name = this.localizedInventoryName(inventory.inventoryName);
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
        key: 'inventoryName',
        label: $localize`:@@inventory.fields.name:Nom de l'inventaire`,
        type: 'string',
        required: true,
        errorMessages: { required: $localize`:@@inventory.validation.nameRequired:Le nom de l'inventaire est requis.` },
      },
      {
        key: 'inventoryType',
        label: $localize`:@@inventory.fields.type:Type d'inventaire`,
        type: 'enum',
        required: true,
        errorMessages: { required: $localize`:@@inventory.validation.typeRequired:Le type d'inventaire est requis.` },
        options: this.inventoryTypes().map((type) => ({ value: type.type, label: this.localizedInventoryType(type.type) })),
      },
      {
        key: 'inventoryDescription',
        label: $localize`:@@inventory.fields.description:Description`,
        type: 'string',
        required: true,
        errorMessages: { required: $localize`:@@inventory.validation.descriptionRequired:La description est requise.` },
      },
    ];
  }

  private async openInventoryDialog(inventory?: Inventory): Promise<void> {
    this.formError.set(null);

    if (this.inventoryTypesLoading() || this.inventoryTypes().length === 0) {
      this.formError.set(
        this.inventoryTypesError() ?? $localize`:@@inventory.errors.typesLoading:Les types d'inventaire sont encore en chargement. Réessayez dans un instant.`,
      );
      return;
    }

    const dialogRef = this.dialog.open<
      ModalForm<InventoryFormValue>,
      ModalFormData<InventoryFormValue>,
      ModalFormResult<InventoryFormValue>
    >(ModalForm, {
      data: {
        title: inventory ? $localize`:@@inventory.actions.editTitle:Modifier l'inventaire` : $localize`:@@inventory.actions.add:Ajouter un inventaire`,
        submitLabel: inventory ? $localize`:@@inventory.actions.saveChanges:Enregistrer les modifications` : $localize`:@@inventory.actions.add:Ajouter un inventaire`,
        cancelLabel: $localize`:@@inventory.actions.cancel:Annuler`,
        fields: this.inventoryFields(),
        initialValue: inventory
          ? {
              inventoryName: this.localizedInventoryName(inventory.inventoryName),
              inventoryType: inventory.inventoryType,
              inventoryDescription: this.localizedInventoryDescription(inventory.inventoryDescription),
            }
          : { inventoryName: '', inventoryType: '', inventoryDescription: '' },
      },
      ariaLabel: inventory ? $localize`:@@inventory.actions.editTitle:Modifier l'inventaire` : $localize`:@@inventory.actions.add:Ajouter un inventaire`,
      width: '480px',
      maxWidth: '95vw',
    });

    // The dialog closes with undefined when the user cancels.
    const result = await firstValueFrom(dialogRef.afterClosed());

    const allowed = inventory ? this.can().canUpdateInventory : this.can().canCreateInventory;

    if (!result || !allowed) return;

    await this.saveInventory(result.value, inventory ?? null);
  }

  private async saveInventory(value: InventoryFormValue, editingInventory: Inventory | null): Promise<void> {
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

      this.loadInventories(this.currentPage());
    } catch {
      this.formError.set(editingId ? $localize`:@@inventory.errors.update:Impossible de modifier l'inventaire.` : $localize`:@@inventory.errors.create:Impossible de créer l'inventaire.`);
    } finally {
      this.savingInventory.set(false);
    }
  }

  private toInventoryRequest(value: InventoryFormValue, original: Inventory | null): InventoryRequest {
    return {
      inventoryName:
        original && value.inventoryName === this.localizedInventoryName(original.inventoryName)
          ? original.inventoryName
          : value.inventoryName,
      inventoryType: value.inventoryType,
      inventoryDescription:
        original &&
        value.inventoryDescription === this.localizedInventoryDescription(original.inventoryDescription)
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
          this.inventories.update((current) =>
            current.filter((inv) => inv.inventoryId !== inventory.inventoryId),
          );
        },
        error: () => {
          this.deleteError.set($localize`:@@inventory.errors.delete:Impossible de supprimer l'inventaire.`);
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

    this.inventorySubscription = this.inventoryResults(requestedPage).subscribe({
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
          this.noResultsMessage.set(this.emptyFilteredMessage());
          this.errorMessage.set(null);
          return;
        }

        this.noResultsMessage.set(null);
        this.errorMessage.set({
          code: isApiError(err) ? err.code : 'UNKNOWN',
          message: $localize`:@@inventory.errors.load:Impossible de charger les inventaires.`,
        });
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
          this.noResultsMessage.set(this.emptyFilteredMessage());
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
