import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Inventory, InventoryProduct, InventoryRequest, ProductRequest } from '../models/inventory.model';
import { environment } from '../../../../environments/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class InventoryApiService {
  private readonly BASE_URL = `${environment.apiUrl}/inventories`;
  private http = inject(HttpClient);

  
  getAllInventories(page?: number, size?: number, searchParams?: any): Observable<Inventory[]> {
    let url = `${this.BASE_URL}`;
    const params: string[] = [];
    
    if (page !== undefined) params.push(`page=${page}`);
    if (size !== undefined) params.push(`size=${size}`);
    
    if (searchParams) {
      if (searchParams.inventoryCode) params.push(`inventoryCode=${searchParams.inventoryCode}`);
      if (searchParams.inventoryName) params.push(`inventoryName=${searchParams.inventoryName}`);
      if (searchParams.inventoryType) params.push(`inventoryType=${searchParams.inventoryType}`);
      if (searchParams.inventoryDescription) params.push(`inventoryDescription=${searchParams.inventoryDescription}`);
    }
    
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }
    
    return this.http.get<Inventory[]>(url);
  }

  
  getInventoryById(inventoryId: string): Observable<Inventory> {
    return this.http.get<Inventory>(`${this.BASE_URL}/${inventoryId}`);
  }

  
  getInventoryTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.BASE_URL}/types`);
  }

  
  createInventoryType(typeName: { name: string }): Observable<any> {
    return this.http.post<any>(`${this.BASE_URL}/types`, typeName);
  }

  
  createInventory(inventory: InventoryRequest): Observable<Inventory> {
    return this.http.post<Inventory>(`${this.BASE_URL}`, inventory);
  }

  
  updateInventory(inventoryId: string, inventory: InventoryRequest): Observable<Inventory> {
    return this.http.put<Inventory>(`${this.BASE_URL}/${inventoryId}`, inventory);
  }

  
  deleteInventory(inventoryId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${inventoryId}`);
  }

  
  getInventoryProducts(inventoryId: string): Observable<InventoryProduct[]> {
    return this.http.get<InventoryProduct[]>(`${this.BASE_URL}/${inventoryId}/products`);
  }

  
  getInventoryProduct(inventoryId: string, productId: string): Observable<InventoryProduct> {
    return this.http.get<InventoryProduct>(
      `${this.BASE_URL}/${inventoryId}/products/${productId}`
    );
  }

 
  addProductToInventory(
    inventoryId: string,
    product: Partial<InventoryProduct>
  ): Observable<InventoryProduct> {
    return this.http.post<InventoryProduct>(
      `${this.BASE_URL}/${inventoryId}/products`,
      product
    );
  }

  
  updateInventoryProduct(
    inventoryId: string,
    productId: string,
    product: Partial<InventoryProduct>
  ): Observable<InventoryProduct> {
    return this.http.put<InventoryProduct>(
      `${this.BASE_URL}/${inventoryId}/products/${productId}`,
      product
    );
  }

  
  deleteInventoryProduct(inventoryId: string, productId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${inventoryId}/products/${productId}`);
  }

  // Additional methods for product management
  getProductById(productId: string): Observable<InventoryProduct> {
    return this.http.get<InventoryProduct>(`${this.BASE_URL}/products/${productId}`);
  }

  createProduct(product: ProductRequest): Observable<InventoryProduct> {
    return this.http.post<InventoryProduct>(`${this.BASE_URL}/${product.inventoryId}/products`, product);
  }

  updateProduct(productId: string, product: ProductRequest): Observable<InventoryProduct> {
    return this.http.put<InventoryProduct>(`${this.BASE_URL}/${product.inventoryId}/products/${productId}`, product);
  }

  deleteProduct(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/products/${productId}`);
  }

  // Delete all inventories method
  deleteAllInventories(): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}`);
  }
}


