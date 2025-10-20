import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product, ProductRequest } from '../models/product.model';
import { ApiConfigService } from '../../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root',
})
export class ProductApiService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  getAllProducts(
    minPrice?: number,
    maxPrice?: number,
    minRating?: number,
    maxRating?: number,
    deliveryType?: string,
    productType?: string
  ): Observable<Product[]> {
    const params: Record<string, unknown> = {};
    if (minPrice !== undefined && minPrice !== null) params.minPrice = minPrice;
    if (maxPrice !== undefined && maxPrice !== null) params.maxPrice = maxPrice;
    if (minRating !== undefined && minRating !== null) params.minRating = minRating;
    if (maxRating !== undefined && maxRating !== null) params.maxRating = maxRating;
    if (deliveryType && deliveryType !== 'default') params.deliveryType = deliveryType;
    if (productType && productType !== 'default') params.productType = productType;

    return this.http
      .get(`${this.apiConfig.getFullUrl('/products')}`, {
        params: params as any,
        responseType: 'text' as any,
        withCredentials: true,
      })
      .pipe(
        map(response => {
          return (response as unknown as string)
            .split('data:')
            .map((dataChunk: string) => {
              try {
                if (dataChunk === '') return null;
                return JSON.parse(dataChunk);
              } catch (error) {
                return null;
              }
            })
            .filter((data: unknown): data is Product => data !== null);
        })
      );
  }

  getProductById(productId: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiConfig.getFullUrl('/products')}/${productId}`);
  }

  createProduct(product: ProductRequest): Observable<Product> {
    return this.http.post<Product>(`${this.apiConfig.getFullUrl('/products')}`, product);
  }

  updateProduct(productId: string, product: ProductRequest): Observable<Product> {
    return this.http.put<Product>(
      `${this.apiConfig.getFullUrl('/products')}/${productId}`,
      product
    );
  }

  deleteProduct(productId: string, cascade?: boolean): Observable<void> {
    const params = cascade ? { cascadeBundles: true } : undefined;
    return this.http.delete<void>(`${this.apiConfig.getFullUrl('/products')}/${productId}`, {
      params,
    });
  }

  searchProducts(searchTerm: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiConfig.getFullUrl('/products')}/search`, {
      params: { q: searchTerm },
      withCredentials: true,
    });
  }

  getProductsByType(productType: string): Observable<Product[]> {
    return this.getAllProducts(undefined, undefined, undefined, undefined, undefined, productType);
  }

  getProductImage(imageId: string): Observable<unknown> {
    return this.http.get(this.apiConfig.getFullUrl(`/images/${imageId}`), {
      withCredentials: true,
    });
  }
}
