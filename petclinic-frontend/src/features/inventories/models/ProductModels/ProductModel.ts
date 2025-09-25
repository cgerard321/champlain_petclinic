import { Status } from '@/features/inventories/models/ProductModels/Status.ts';

export interface ProductModel {
  productId: string;
  productName: string;
  productDescription: string;
  productPrice: number; // Cost(Regular) price
  productSalePrice: number; // Selling price
  productQuantity: number;
  productMargin: number;
  status: Status;
  requestCount: number;
}
