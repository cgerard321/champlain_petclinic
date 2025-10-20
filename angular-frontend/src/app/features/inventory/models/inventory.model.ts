export interface Inventory {
  inventoryId: string;
  inventoryCode: string;
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  recentUpdateMessage?: string;
  isTemporarilyDeleted?: boolean;
}

export interface InventoryProduct {
  productId: string;
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice?: number;
  isTemporarilyDeleted?: boolean;
}

export interface InventoryRequest {
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
}

export interface ProductRequest {
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice?: number;
  inventoryId: string;
}

export interface PaginatedInventories {
  inventories: Inventory[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}
