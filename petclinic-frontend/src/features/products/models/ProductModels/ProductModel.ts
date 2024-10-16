export interface ProductModel {
  productId: string;
  imageId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number;
  productQuantity: number;
  productStatus: 'PRE_ORDER' | 'AVAILABLE' | 'OUT_OF_STOCK' ;
  requestCount: number;
  productType: string;
  dateAdded: Date;
  releaseDate?: Date; 
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
  productType: '',
  dateAdded: new Date(),
  releaseDate: undefined
};