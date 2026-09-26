import { SseClient } from 'ngx-sse-client';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Inventory } from '@features/inventory/models/inventory.model';

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
      );
  }

  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }
}
