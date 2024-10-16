export interface ProductModel {
  productId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  productQuantity: number;
  status: 'PRE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE';
  requestCount: number;
}
