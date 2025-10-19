import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Inventory, InventoryProduct, InventoryRequest, ProductRequest } from '../models/inventory.model';
import { ApiConfigService } from '../../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root'
})
export class InventoryApiService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  
  getAllInventories(page?: number, size?: number, searchParams?: any): Observable<Inventory[]> {
    let url = `${this.apiConfig.getFullUrl('/inventories')}`;
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
    return this.http.get<Inventory>(`${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}`);
  }

  
  getInventoryTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiConfig.getFullUrl('/inventories')}/types`);
  }

  
  createInventoryType(typeName: { name: string }): Observable<any> {
    return this.http.post<any>(`${this.apiConfig.getFullUrl('/inventories')}/types`, typeName);
  }

  
  createInventory(inventory: InventoryRequest): Observable<Inventory> {
    return this.http.post<Inventory>(`${this.apiConfig.getFullUrl('/inventories')}`, inventory);
  }

  
  updateInventory(inventoryId: string, inventory: InventoryRequest): Observable<Inventory> {
    return this.http.put<Inventory>(`${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}`, inventory);
  }

  
  deleteInventory(inventoryId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}`);
  }

  
  getInventoryProducts(inventoryId: string): Observable<InventoryProduct[]> {
    return this.http.get<InventoryProduct[]>(`${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}/products`);
  }

  
  getInventoryProduct(inventoryId: string, productId: string): Observable<InventoryProduct> {
    return this.http.get<InventoryProduct>(
      `${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}/products/${productId}`
    );
  }

 
  addProductToInventory(
    inventoryId: string,
    product: Partial<InventoryProduct>
  ): Observable<InventoryProduct> {
    return this.http.post<InventoryProduct>(
      `${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}/products`,
      product
    );
  }

  
  updateInventoryProduct(
    inventoryId: string,
    productId: string,
    product: Partial<InventoryProduct>
  ): Observable<InventoryProduct> {
    return this.http.put<InventoryProduct>(
      `${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}/products/${productId}`,
      product
    );
  }

  
  deleteInventoryProduct(inventoryId: string, productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiConfig.getFullUrl('/inventories')}/${inventoryId}/products/${productId}`);
  }

  // Additional methods for product management
  getProductById(productId: string): Observable<InventoryProduct> {
    return this.http.get<InventoryProduct>(`${this.apiConfig.getFullUrl('/inventories')}/products/${productId}`);
  }

  createProduct(product: ProductRequest): Observable<InventoryProduct> {
    return this.http.post<InventoryProduct>(`${this.apiConfig.getFullUrl('/inventories')}/${product.inventoryId}/products`, product);
  }

  updateProduct(productId: string, product: ProductRequest): Observable<InventoryProduct> {
    return this.http.put<InventoryProduct>(`${this.apiConfig.getFullUrl('/inventories')}/${product.inventoryId}/products/${productId}`, product);
  }

  deleteProduct(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiConfig.getFullUrl('/inventories')}/products/${productId}`);
  }

  // Delete all inventories method
  deleteAllInventories(): Observable<void> {
    return this.http.delete<void>(`${this.apiConfig.getFullUrl('/inventories')}`);
  }
}


