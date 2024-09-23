import {Status} from "@/features/inventories/models/ProductModels/Status.ts";
export interface ProductModel {
  productId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  productQuantity: number;
  status: Status;
}
