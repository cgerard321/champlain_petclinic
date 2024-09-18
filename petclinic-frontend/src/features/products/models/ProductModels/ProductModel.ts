export interface ProductModel {
  productId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number;
  productQuantity: number;
  status: 'RE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE';
}