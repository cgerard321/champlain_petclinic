import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SseClient } from 'ngx-sse-client';
import { Observable, timer } from 'rxjs';
import { filter, map, retry } from 'rxjs/operators';

import { Inventory } from '@features/inventories/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly sse = inject(SseClient);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/inventories';

  getInventories(): Observable<Inventory> {
    return this.sse
      .stream(this.baseUrl, { keepAlive: false, responseType: 'event' }, {}, 'GET')
      .pipe(
        filter((event): event is MessageEvent => event.type !== 'error'),
        map((event) => JSON.parse((event as MessageEvent).data) as Inventory),

        // Reconnect after an SSE error.
        retry({ count: Infinity, delay: () => timer(5000) }),
      );
  }

  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }
}
