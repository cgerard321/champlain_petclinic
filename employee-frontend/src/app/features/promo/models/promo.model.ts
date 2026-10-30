import { FileDetails } from '@features/prod/models/image.model';

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
  automatic?: boolean;
  minimumPurchaseAmount?: number | null;
}

export interface PromoRequest {
  image?: FileDetails;
  removeImage?: boolean;
  name: string;
  code: string;
  discount: number;
  expirationDate: string;
  active: boolean;
  scope?: 'CART' | 'PRODUCT' | 'PRODUCT_TYPE';
  productIds?: string[];
  productTypes?: string[];
  automatic?: boolean;
  minimumPurchaseAmount?: number | null;
}
