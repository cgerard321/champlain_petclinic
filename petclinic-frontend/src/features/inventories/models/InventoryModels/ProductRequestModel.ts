import {Status} from "@/features/inventories/models/ProductModels/Status.ts";

export interface ProductRequestModel {
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  status?: Status;
}
