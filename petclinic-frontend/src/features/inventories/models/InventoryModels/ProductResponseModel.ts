export interface ProductResponseModel {
  id: string;
  productId: string;
  inventoryId: string;
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  status: 'PRE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE';
}
