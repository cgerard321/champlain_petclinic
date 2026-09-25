import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription, timer, switchMap } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { InventoryService } from '@features/inventory/services/inventory-service';
import { Inventory } from '@features/inventory/models/inventory.model';
import { isApiError } from '@core/models/api-error';

const POLL_INTERVAL_MS = 15_000; // Interval in which the page automatically refreshes in milliseconds

@Component({
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  selector: 'app-inventory-list',
  styleUrl: './inventory-list.css',
  templateUrl: './inventory-list.html',
})
export class InventoryList implements OnInit, OnDestroy {
  private readonly inventoryService = inject(InventoryService);
  private pollSubscription?: Subscription; // A subscription represents a disposable value, such as the exectution of an Observable

  protected readonly inventories = signal<Inventory[]>([]);
  protected readonly quantities = signal<Record<string, number | null>>({});
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  // Runs when the page is opened
  ngOnInit(): void {
    // timer(0, interval) fires immediately and then on the POLL_INTERVAL_MS
    this.pollSubscription = timer(0, POLL_INTERVAL_MS)
      .pipe(switchMap(() => this.inventoryService.getInventories()))
      .subscribe({
        next: (inventories) => {
          this.inventories.set(inventories);
          this.isLoading.set(false);
          this.errorMessage.set(null);
          this.loadQuantities(inventories);
        },
        error: (err: unknown) => {
          this.isLoading.set(false);
          this.errorMessage.set(isApiError(err) ? err.message : 'Could not load inventories.');
        },
      });
  }

  // Runs when the page is closed
  ngOnDestroy(): void {
    // .unsubsribe() disposes of the value held by a Subscription
    this.pollSubscription?.unsubscribe();
  }

  // Tries to return the .quantities value for a specified inventory, returns null if it fails
  protected quantityFor(inventoryId: string): number | null {
    return this.quantities()[inventoryId] ?? null;
  }

  // Helper method that loads the stock for each individual inventory
  private loadQuantities(inventories: Inventory[]): void {
    inventories.forEach((inventory) => {
      this.inventoryService.getQuantity(inventory.inventoryId).subscribe({
        next: (quantity) =>
          this.quantities.update((current) => ({ ...current, [inventory.inventoryId]: quantity })),
        // Some roles (VET) aren't authorized to read quantities — fail
        // quietly for that one card instead of breaking the page.
        error: () =>
          this.quantities.update((current) => ({ ...current, [inventory.inventoryId]: null })),
      });
    });
  }
}
