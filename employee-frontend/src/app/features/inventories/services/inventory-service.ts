import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SseClient } from 'ngx-sse-client';
import { Observable, throwError, timer } from 'rxjs';
import { filter, map, retry } from 'rxjs/operators';

import { Inventory } from '@features/inventories/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/inventories';

  getInventories(importantOnly = false): Observable<Inventory> {
    // The gateway only filters when importantOnly is true, so the parameter is
    // left out entirely when the filter is off and the default search is used.
    let params = new HttpParams();

    if (importantOnly) {
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
            const status = error instanceof HttpErrorResponse ? error.status : undefined;

            // Only a dropped connection is worth retrying; permission and
            // server errors are forwarded to the page instead.
            return status === 0 ? timer(5000) : throwError(() => error);
          },
        }),
      );
  }

  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }

  updateImportantStatus(inventoryId: string, important: boolean): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${inventoryId}/important`, { important });
  }
}
