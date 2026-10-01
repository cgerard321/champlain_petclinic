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

@Injectable({ providedIn: 'root' })
export class InventoryService {
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
  updateImportantStatus(inventoryId: string, important: boolean): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${inventoryId}/important`, { important });
  }
}
