import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, timer } from 'rxjs';
import { filter, map, retry } from 'rxjs/operators';

import { SseClient } from '@core/services/sse-client';
import { Inventory, InventoryRequest } from '@features/inventories/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/inventories';

  getInventories(): Observable<Inventory> {
    return this.sse
      .stream(this.baseUrl, { keepAlive: false, responseType: 'event' }, {}, 'GET')
      .pipe(
        filter(
          (event): event is MessageEvent =>
            event instanceof MessageEvent && event.type !== 'error',
        ),
        map((event) => JSON.parse(event.data) as Inventory),
        retry({ count: Infinity, delay: () => timer(5000) }),
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
      .stream(`${this.baseUrl}/${inventoryId}`, { keepAlive: false, responseType: 'event' }, {}, 'GET')
      .pipe(
        filter(
          (event): event is MessageEvent =>
            event instanceof MessageEvent && event.type !== 'error',
        ),
        map((event) => JSON.parse(event.data) as Inventory),
        retry({ count: Infinity, delay: () => timer(5000) }),
      );
  }
}