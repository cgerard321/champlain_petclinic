import { Status } from '@/features/inventories/models/ProductModels/Status.ts';

export interface ProductResponseModel {
  id: string;
  productId: string;
  inventoryId: string;
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  status: Status;
}
