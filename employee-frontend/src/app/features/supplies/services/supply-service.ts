import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { filter, map, Observable, toArray } from 'rxjs';

import { SseClient } from '@core/services/sse-client';
import { Supply } from '@features/supplies/models/supply';

@Injectable({ providedIn: 'root' })
export class SupplyService {
  private readonly http = inject(HttpClient);
  private readonly sse = inject(SseClient);

  getSupplies(inventoryId: string): Observable<Supply[]> {
    // This endpoint returns a finite SSE snapshot; collect it before giving the page one Supply[] to display.
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
        toArray(),
      );
  }

  createSupply(
    inventoryId: string,
    supply: {
      productName: string;
      productDescription: string;
      productPrice: number;
      productQuantity: number;
      productSalePrice: number;
    },
  ): Observable<Supply> {
    return this.http.post<Supply>(`/api/gateway/inventories/${inventoryId}/products`, supply);
  }

  updateSupply(
    inventoryId: string,
    productId: string,
    supply: {
      productName: string;
      productDescription: string;
      productPrice: number;
      productQuantity: number;
      productSalePrice: number;
    },
  ): Observable<Supply> {
    return this.http.put<Supply>(
      `/api/gateway/inventories/${inventoryId}/products/${productId}`,
      supply,
    );
  }

  deleteSupply(inventoryId: string, productId: string): Observable<void> {
    return this.http.delete<void>(`/api/gateway/inventories/${inventoryId}/products/${productId}`);
  }
}
