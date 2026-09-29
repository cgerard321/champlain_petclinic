import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, throwError, timer } from 'rxjs';
import { filter, map, retry } from 'rxjs/operators';

import { SseClient } from '@core/services/sse-client';
import { Inventory } from '@features/inventories/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/inventories';

  getInventories(): Observable<Inventory> {
    return this.sse.stream(this.baseUrl, { keepAlive: false }).pipe(
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
}
