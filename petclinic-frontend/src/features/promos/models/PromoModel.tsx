import { FileDetails } from '@/shared/models/FileDetails';

export interface Promo {
  imageId?: string;
  image?: FileDetails | null;
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
