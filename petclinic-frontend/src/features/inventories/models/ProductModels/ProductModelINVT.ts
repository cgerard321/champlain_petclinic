import {Status} from "@/features/inventories/models/ProductModels/Status.ts";

export interface ProductModelINVT {
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  productStatus: Status;
}
