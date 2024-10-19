import { DeliverType } from '@/features/products/models/ProductModels/DeliverType.ts';

export interface ProductModel {
  productId: string;
  imageId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number;
  productQuantity: number;
  status: 'RE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE';
  requestCount: number;
  productType: string;
  deliveryType:
    | 'DELIVERY'
    | 'PICKUP'
    | 'DELIVERY_AND_PICKUP'
    | 'NO_DELIVERY_OPTION';
}

export const emptyProductModel: ProductModel = {
  productId: '',
  imageId: '',
  productName: '',
  productDescription: '',
  productSalePrice: 0,
  averageRating: 0,
  productQuantity: 0,
  status: 'OUT_OF_STOCK',
  requestCount: 0,
  productType: '',
  deliveryType: DeliverType.NO_DELIVERY_OPTION,
  // deliveryType: 'NO_DELIVERY_OPTION'
};
