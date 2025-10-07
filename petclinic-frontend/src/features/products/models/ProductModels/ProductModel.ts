import { DeliverType } from '@/features/products/models/ProductModels/DeliverType.ts';
import { ProductType } from '@/features/products/models/ProductModels/ProductType.ts';

export interface ProductModel {
  productId: string;
  imageId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number;
  productQuantity: number;
  productStatus: 'PRE_ORDER' | 'AVAILABLE' | 'OUT_OF_STOCK';
  requestCount: number;
  productType: 'FOOD' | 'MEDICATION' | 'ACCESSORY' | 'EQUIPMENT';
  isUnlisted: boolean;
  dateAdded: Date;
  releaseDate?: Date;
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
  productStatus: 'OUT_OF_STOCK',
  requestCount: 0,
  productType: ProductType.ACCESSORY,
  isUnlisted: false,
  dateAdded: new Date(),
  releaseDate: undefined,
  deliveryType: DeliverType.NO_DELIVERY_OPTION,
};
