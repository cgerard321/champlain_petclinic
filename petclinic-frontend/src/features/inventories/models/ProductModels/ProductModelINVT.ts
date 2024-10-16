export interface ProductModelINVT {
  productName: string;
  productDescription: string;
  productPrice: number;
  productQuantity: number;
  productSalePrice: number;
  status: 'PRE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE';
}
