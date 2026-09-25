import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Inventory } from '@features/inventory/models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/gateway/inventories';

  // GETs the full inventory list
  getInventories(): Observable<Inventory[]> {
    return this.http
      .get(this.baseUrl, { responseType: 'text' })
      .pipe(map((raw) => this.parseSseEvents<Inventory>(raw)));
  }

  // GETs current stock count for one inventory
  getQuantity(inventoryId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${inventoryId}/productquantity`);
  }

  // Helper method used to Parse SSE Events - Could be moved to a shared file later if needed
  private parseSseEvents<T>(raw: string): T[] {
    return raw
      .split(/\r?\n\r?\n/)
      .map((block) =>
        block
          .split(/\r?\n/)
          .filter((line) => line.startsWith('data:'))
          .map((line) => line.slice(5).trim())
          .join('\n'),
      )
      .filter((jsonText) => jsonText.length > 0)
      .map((jsonText) => JSON.parse(jsonText) as T);
  }
}
