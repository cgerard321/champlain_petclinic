import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { filter, map, Observable, retry, throwError, timer } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import {
  Inventory,
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

  getInventories(
    filters: Partial<InventoryFilters> = {},
    page = 0,
    size = 10,
  ): Observable<Inventory> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));

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
                : err.code === 0 || err.status === 0;

            return isNetworkError ? timer(5000) : throwError(() => error);
          },
        }),
      );
  }

  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }
}
