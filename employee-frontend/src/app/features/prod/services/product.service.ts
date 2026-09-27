import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

import { Product, ProductEnums, ProductRequest } from '@features/prod/models/product.model';

export interface ProductFilters {
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  maxRating?: number;
  sort?: string;
  deliveryType?: string;
  productType?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);

  getProducts(filters: ProductFilters = {}): Observable<Product[]> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(filters)) {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    }

    return this.http
      .get('/api/gateway/products', { params, responseType: 'text' })
      .pipe(map((payload) => this.parseProductStream(payload)));
  }

  getProduct(productId: string): Observable<Product> {
    return this.http.get<Product>(`/api/gateway/products/${productId}`);
  }

  createProduct(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>('/api/gateway/products', request);
  }

  getProductEnums(): Observable<ProductEnums> {
    return this.http.get<ProductEnums>('/api/gateway/products/enums');
  }

  private parseProductStream(payload: string): Product[] {
    const products: Product[] = [];
    const records = payload.split(/\r?\n\r?\n/);

    for (const record of records) {
      const dataLine = record
        .split(/\r?\n/)
        .find((line) => line.trimStart().startsWith('data:'));

      if (!dataLine) {
        continue;
      }

      const json = dataLine.slice(dataLine.indexOf(':') + 1).trim();
      if (!json || json === '[DONE]') {
        continue;
      }

      try {
        products.push(JSON.parse(json) as Product);
      } catch {
        throw new Error('The Product API returned an invalid event-stream record.');
      }
    }

    return products;
  }
}
