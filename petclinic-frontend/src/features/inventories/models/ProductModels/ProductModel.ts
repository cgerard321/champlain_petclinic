export interface ProductModel {
  productId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  productQuantity: number;
  status: 'RE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE';
  requestCount: number;
}
