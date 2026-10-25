export interface ProductModel {
  promoLookupFailed?: boolean;
  discountAmount?: number;
  productId: string;
  imageId: string;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating?: number;
  quantity?: number;
  productQuantity: number;
}
