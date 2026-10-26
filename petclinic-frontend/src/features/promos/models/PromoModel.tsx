export interface Promo {
  id: string;
  name: string;
  code: string;
  discount: number;
  expirationDate: string;
  active: boolean;
  scope?: 'CART' | 'PRODUCT' | 'PRODUCT_TYPE';
  productIds?: string[];
  productTypes?: string[];
}
