import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { isApiError, ApiError } from '@core/models/api-error';
import { Inventory } from '@features/inventories/models/inventory.model';
import { InventoryService } from '@features/inventories/services/inventory-service';
import { getInventoryPermissions } from '@shared/models/inventory-permissions';
import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule],
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
  protected readonly can = computed(() => getInventoryPermissions(this.authState.roles()));

  ngOnInit(): void {


    // Added — Receptionist has no live gateway access to GET "" (the SSE
    // stream this subscribes to). Don't open the connection at all — that's
    // the "hidden or disabled in the UI" half of the requirement, on top
    // of the backend's guaranteed 403 if it were opened anyway.
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

    // Added — GET .../productquantity is ADMIN/INVENTORY_MANAGER only.
    // Without this guard, VET fires one failed 403 request per item as
    // it streams in, forever (SSE keeps the connection open, so this is
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
}
