import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { FormField, form, required, submit } from '@angular/forms/signals';

import { isApiError, ApiError } from '@core/models/api-error';
import { Inventory, InventoryRequest, INVENTORY_TYPES } from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';
import { getInventoryPermissions } from '@shared/models/inventory-permissions';
import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule, FormField],
  selector: 'app-inventory-list',
  styleUrls: ['../../inventories.css', './inventory-list.css'],
  templateUrl: './inventory-list.html',
})
export class InventoryList implements OnInit, OnDestroy {
  private readonly inventoryService = inject(InventoryService);
  private inventorySubscription?: Subscription;
  private readonly quantitySubscriptions = new Set<Subscription>();

  protected readonly inventories = signal<Inventory[]>([]);
  protected readonly quantities = signal<Record<string, number | null>>({});
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<ApiError | null>(null);

  private readonly authState = inject(AuthState);
  // this tells the page what the current user is allowed to do (view/create/edit/delete)
  protected readonly can = computed(() => getInventoryPermissions(this.authState.roles()));

  // form + state for the Add/Edit Inventory form
  // keeping it as a plain inline form for now, not a modal, since the modal
  // ticket (Lazaro's) is supposed to wrap an existing form, and there wasn't
  // one yet — this is that form
  protected readonly showAddForm = signal(false);
  protected readonly editingInventoryId = signal<string | null>(null);
  protected readonly savingInventory = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected readonly newInventory = signal({
    inventoryName: '',
    inventoryType: '',
    inventoryDescription: '',
  });

  protected readonly inventoryForm = form(this.newInventory, (path) => {
    required(path.inventoryName, { message: 'Name is required' });
    required(path.inventoryType, { message: 'Type is required' });
    required(path.inventoryDescription, { message: 'Description is required' });
  });

  ngOnInit(): void {
    // receptionist doesn't have access to any inventory data right now (checked
    // the gateway's @SecuredEndpoint roles), so don't even open the stream for them
    if (!this.can().hasAnyAccess) {
      this.isLoading.set(false);
      return;
    }

    this.inventorySubscription = this.inventoryService.getInventories().subscribe({
      next: (item) => {
        this.isLoading.set(false);
        this.errorMessage.set(null);

        this.inventories.update((current) => {
          const idx = current.findIndex((inv) => inv.inventoryId === item.inventoryId);

          // If the inventory is new, add it to the list
          if (idx === -1) return [...current, item];

          // If the inventory already exists, replace it
          const updated = [...current];
          updated[idx] = item;
          return updated;
        });
        this.loadQuantities(item.inventoryId);
      },
      error: (err: unknown) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          isApiError(err) ? err : { code: 'UNKNOWN', message: 'Failed to load inventories.' },
        );
      },
      complete: () => this.isLoading.set(false),
    });
  }

  ngOnDestroy(): void {
    this.inventorySubscription?.unsubscribe();
    this.quantitySubscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
  }

  protected quantityFor(inventoryId: string): number | null {
    return this.quantities()[inventoryId] ?? null;
  }

  private loadQuantities(inventoryId: string): void {
    // Initializes the quantity the first time the inventory is loaded
    // Otherwise, keeps displaying the existing value
    if (!(inventoryId in this.quantities())) {
      this.quantities.update((current) => ({
        ...current,
        [inventoryId]: null,
      }));
    }

    // the quantity endpoint only works for admin/inventory manager on the
    // backend, so don't bother calling it for vet — it would just 403 every
    // single time an item comes in from the stream
    if (!this.can().canViewProductQuantity) {
      return;
    }

    const subscription = this.inventoryService.getQuantity(inventoryId).subscribe({
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
      complete: () => {
        this.quantitySubscriptions.delete(subscription);
      },
    });
    this.quantitySubscriptions.add(subscription);
  }

  // form actions below

  protected toggleAddForm(): void {
    this.showAddForm.update((visible) => !visible);
    if (!this.showAddForm()) {
      this.editingInventoryId.set(null);
      this.formError.set(null);
    }
  }

  protected editInventory(inventory: Inventory): void {
    if (!this.can().canUpdateInventory) return;

    this.editingInventoryId.set(inventory.inventoryId);
    this.newInventory.set({
      inventoryName: inventory.inventoryName,
      inventoryType: inventory.inventoryType,
      inventoryDescription: inventory.inventoryDescription,
    });
    this.showAddForm.set(true);
  }

  protected saveInventory(event: Event): void {
    event.preventDefault();

    void submit(this.inventoryForm, async () => {
      this.savingInventory.set(true);
      this.formError.set(null);

      const editingId = this.editingInventoryId();
      const body = this.newInventory();

      const request$ = editingId
        ? this.inventoryService.updateInventory(editingId, body)
        : this.inventoryService.createInventory(body);

      request$.subscribe({
        next: (saved) => {
          // updating the list right here instead of waiting for the SSE stream
          // to reconnect (it can take a few seconds), so the change shows up
          // on screen immediately like the ticket asks for
          this.inventories.update((current) => {
            const idx = current.findIndex((inv) => inv.inventoryId === saved.inventoryId);
            if (idx === -1) return [...current, saved];
            const updated = [...current];
            updated[idx] = saved;
            return updated;
          });

          this.newInventory.set({
            inventoryName: '',
            inventoryType: INVENTORY_TYPES[0],
            inventoryDescription: '',
          });
          this.editingInventoryId.set(null);
          this.showAddForm.set(false);
          this.savingInventory.set(false);
        },
        error: () => {
          this.formError.set(
            editingId ? 'Unable to update inventory.' : 'Unable to create inventory.',
          );
          this.savingInventory.set(false);
        },
      });
    });
  }

  protected deleteInventoryAction(inventory: Inventory): void {
    if (!this.can().canDeleteInventory) return;

    const confirmed = window.confirm(`Delete ${inventory.inventoryName}?`);
    if (!confirmed) return;

    this.inventoryService.deleteInventory(inventory.inventoryId).subscribe({
      next: () => {
        // same as above, remove it from the list right away instead of
        // waiting for the stream to catch up
        this.inventories.update((current) =>
          current.filter((inv) => inv.inventoryId !== inventory.inventoryId),
        );
      },
      error: () => {
        this.formError.set('Unable to delete inventory.');
      },
    });
  }
}