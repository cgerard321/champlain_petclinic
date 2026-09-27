import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

import { Supply } from '@features/supplies/models/supply';

@Injectable({ providedIn: 'root' })
export class SupplyService {
  private readonly http = inject(HttpClient);

  getSupplies(inventoryId: string): Observable<Supply[]> {
    return this.http
      .get(`/api/gateway/inventories/${inventoryId}/products`, {
        responseType: 'text',
      })
      .pipe(
        map((response) =>
          response
            .split('\n')
            .filter((line) => line.startsWith('data:'))
            .map((line) => JSON.parse(line.substring(5)) as Supply),
        ),
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
