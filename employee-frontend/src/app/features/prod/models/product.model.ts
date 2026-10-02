import { FileDetails } from '@features/prod/models/image.model';
export interface ProductType {
  productTypeId: string;
  typeName: string;
}

export enum ProductStatus {
  AVAILABLE = 'AVAILABLE',
  PRE_ORDER = 'PRE_ORDER',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
}

export enum DeliveryType {
  DELIVERY = 'DELIVERY',
  PICKUP = 'PICKUP',
  DELIVERY_AND_PICKUP = 'DELIVERY_AND_PICKUP',
  NO_DELIVERY_OPTION = 'NO_DELIVERY_OPTION',
}

export interface Product {
  productId: string;
  imageId?: string;
  image?: FileDetails | null;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating?: number;
  requestCount?: number;
  productQuantity: number;
  isUnlisted: boolean;
  productType: string;
  productTypeId: string;
  productStatus: ProductStatus;
  deliveryType: DeliveryType;
  releaseDate?: string;
}

export interface ProductRequest {
  image?: FileDetails;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  productQuantity: number;
  isUnlisted: boolean;
  productTypeId: string;
  releaseDate?: string;
  productStatus?: ProductStatus;
  deliveryType: DeliveryType;
}

export interface ProductEnums {
  productType: ProductType[];
  productStatus: ProductStatus[];
  deliveryType: DeliveryType[];
}
