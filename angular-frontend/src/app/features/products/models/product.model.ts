export interface Product {
  productId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  productQuantity: number;
  productType: 'FOOD' | 'MEDICATION' | 'ACCESSORY' | 'EQUIPMENT';
  productStatus: 'PRE_ORDER' | 'AVAILABLE' | 'OUT_OF_STOCK';
  averageRating: number;
  imageId: string;
  imageData?: string;
  imageType?: string;
  requestCount: number;
  isUnlisted: boolean;
  isTemporarilyDeleted?: boolean;
  dateAdded: Date;
  releaseDate?: Date;
  deliveryType: 'DELIVERY' | 'PICKUP' | 'DELIVERY_AND_PICKUP' | 'NO_DELIVERY_OPTION';
}

export interface ProductRequest {
  productName: string;
  productDescription: string;
  productSalePrice: number;
  productQuantity: number;
  productType: 'FOOD' | 'MEDICATION' | 'ACCESSORY' | 'EQUIPMENT';
  productStatus?: 'PRE_ORDER' | 'AVAILABLE' | 'OUT_OF_STOCK';
  averageRating?: number;
  imageId?: string;
  requestCount?: number;
  isUnlisted?: boolean;
  dateAdded?: Date;
  releaseDate?: Date;
  deliveryType?: 'DELIVERY' | 'PICKUP' | 'DELIVERY_AND_PICKUP' | 'NO_DELIVERY_OPTION';
}

export interface PaginatedProducts {
  products: Product[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}
