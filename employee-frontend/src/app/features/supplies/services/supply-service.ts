import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { filter, map, Observable } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import { Supply, SupplyAuditEntry, SupplyRequest } from '@features/supplies/models/supply';

@Injectable({ providedIn: 'root' })
export class SupplyService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);
  private readonly baseUrl = '/api/gateway/inventories';

  getSupplies(inventoryId: string): Observable<Supply> {
    return this.sse
      .stream(
        `/api/gateway/inventories/${inventoryId}/products`,
        { keepAlive: false, responseType: 'event' },
        {},
        'GET',
      )
      .pipe(
        filter((event): event is MessageEvent => event.type !== 'error'),
        map((event) => JSON.parse(event.data) as Supply),
      );
  }

  createSupply(inventoryId: string, supply: SupplyRequest): Observable<Supply> {
    return this.http.post<Supply>(`${this.baseUrl}/${inventoryId}/products`, supply);
  }

  updateSupply(inventoryId: string, productId: string, supply: SupplyRequest): Observable<Supply> {
    return this.http.put<Supply>(`${this.baseUrl}/${inventoryId}/products/${productId}`, supply);
  }

  deleteSupply(inventoryId: string, productId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${inventoryId}/products/${productId}`);
  }

  consumeSupply(inventoryId: string, productId: string): Observable<Supply> {
    return this.http.patch<Supply>(
      `${this.baseUrl}/${inventoryId}/products/${productId}/consume`,
      {},
    );
  }

  getSupplyHistory(inventoryId: string, productId: string): Observable<SupplyAuditEntry[]> {
    return this.http.get<SupplyAuditEntry[]>(
      `${this.baseUrl}/${inventoryId}/products/${productId}/history`,
    );
  }

  downloadSupplyPdf(inventoryId: string, language = 'en'): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${inventoryId}/products/download`, {
      responseType: 'blob',
      params: { lang: language },
    });
  }
}
