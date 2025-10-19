import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product, ProductRequest } from '../models/product.model';
import { environment } from '../../../../environments/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class ProductApiService {
  private readonly BASE_URL = `${environment.apiUrl}/products`;
  private http = inject(HttpClient);

  
  getAllProducts(
    minPrice?: number,
    maxPrice?: number,
    minRating?: number,
    maxRating?: number,
    deliveryType?: string,
    productType?: string
  ): Observable<Product[]> {
    const params: any = {};
    if (minPrice !== undefined && minPrice !== null) params.minPrice = minPrice;
    if (maxPrice !== undefined && maxPrice !== null) params.maxPrice = maxPrice;
    if (minRating !== undefined && minRating !== null) params.minRating = minRating;
    if (maxRating !== undefined && maxRating !== null) params.maxRating = maxRating;
    if (deliveryType && deliveryType !== 'default') params.deliveryType = deliveryType;
    if (productType && productType !== 'default') params.productType = productType;

    return this.http.get(`${this.BASE_URL}`, { 
      params,
      responseType: 'text',
      withCredentials: true 
    }).pipe(
      map(response => {
        return response
          .split('data:')
          .map((dataChunk: string) => {
            try {
              if (dataChunk === '') return null;
              return JSON.parse(dataChunk);
            } catch (error) {
              return null;
            }
          })
          .filter((data: any) => data !== null);
      })
    );
  }

  
  getProductById(productId: string): Observable<Product> {
    return this.http.get<Product>(`${this.BASE_URL}/${productId}`);
  }

  
  createProduct(product: ProductRequest): Observable<Product> {
    return this.http.post<Product>(`${this.BASE_URL}`, product);
  }

  
  updateProduct(productId: string, product: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.BASE_URL}/${productId}`, product);
  }

  
  deleteProduct(productId: string, cascade?: boolean): Observable<void> {
    const params = cascade ? { cascadeBundles: true } : undefined;
    return this.http.delete<void>(`${this.BASE_URL}/${productId}`, { params });
  }

  searchProducts(searchTerm: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.BASE_URL}/search`, {
      params: { q: searchTerm },
      withCredentials: true
    });
  }

  getProductsByType(productType: string): Observable<Product[]> {
    return this.getAllProducts(undefined, undefined, undefined, undefined, undefined, productType);
  }

  getProductImage(imageId: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/images/${imageId}`, {
      withCredentials: true
    });
  }
}

