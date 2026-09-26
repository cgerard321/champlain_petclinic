import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { InventoryService } from '@features/inventory/services/inventory-service';
import { Inventory } from '@features/inventory/models/inventory.model';
import { isApiError, ApiError } from '@core/models/api-error';

@Component({
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  selector: 'app-inventory-list',
  styleUrl: './inventory-list.css',
  templateUrl: './inventory-list.html',
})
export class InventoryList implements OnInit, OnDestroy {
  private readonly inventoryService = inject(InventoryService);
  private pollSubscription?: Subscription;

  protected readonly inventories = signal<Inventory[]>([]);
  protected readonly quantities = signal<Record<string, number | null>>({});
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<ApiError | null>(null);

  ngOnInit(): void {
    this.pollSubscription = this.inventoryService.getInventories().subscribe({
      next: (item) => {
        this.isLoading.set(false);
        this.inventories.update((current) => {
          const idx = current.findIndex((inv) => inv.inventoryId === item.inventoryId);
          if (idx === -1) return [...current, item];
          const copy = [...current];
          copy[idx] = item;
          return copy;
        });
        this.loadQuantities([item]);
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
    this.pollSubscription?.unsubscribe();
  }

  protected quantityFor(inventoryId: string): number | null {
    return this.quantities()[inventoryId] ?? null;
  }

  private loadQuantities(inventories: Inventory[]): void {
    inventories.forEach((inventory) => {
      this.inventoryService.getQuantity(inventory.inventoryId).subscribe({
        next: (quantity) =>
          this.quantities.update((current) => ({ ...current, [inventory.inventoryId]: quantity })),

        error: () =>
          this.quantities.update((current) => ({ ...current, [inventory.inventoryId]: null })),
      });
    });
  }
}
