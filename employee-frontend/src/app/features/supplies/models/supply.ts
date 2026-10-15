import { Status } from '@features/supplies/models/status';

export interface Supply {
  productId: string;
  inventoryId: string;
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  status: Status;
  lastUpdatedAt: string;
  photoData?: string | null;
  photoType?: string | null;
  supplierName?: string | null;
  expirationDate?: string | null;
}

export type SupplyAuditAction = 'CREATED' | 'QUANTITY_UPDATED' | 'CONSUMED' | 'DELETED';

export interface SupplyAuditEntry {
  id: string;
  inventoryId: string;
  supplyId: string;
  supplyName: string;
  previousQuantity: number;
  newQuantity: number;
  quantityChange: number;
  action: SupplyAuditAction;
  changedAt: string;
  changedBy: string;
}

export interface SupplyRequest {
  productName: string;
  supplierName?: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  photoData?: string;
  photoType?: string | null;
  expirationDate?: string | null;
}
