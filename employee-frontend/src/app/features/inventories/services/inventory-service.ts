import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SseClient } from 'ngx-sse-client';
import { Observable, throwError, timer } from 'rxjs';
import { filter, map, retry } from 'rxjs/operators';

import {
  Inventory,
  InventoryFilters,
  InventoryType,
} from '@features/inventories/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient);
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
        map((event) => JSON.parse((event as MessageEvent).data) as Inventory),
        retry({
          count: 2,
          delay: (err: unknown) => {
            if (err instanceof HttpErrorResponse && err.status >= 400 && err.status < 500) {
              return throwError(() => err);
            }

            return timer(5000);
          },
        }),
      );
  }

  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }
}
