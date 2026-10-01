import { ProductTypeModel } from './ProductTypeModel';

export interface ProductEnumsModel {
  productType: ProductTypeModel[];
  productStatus: string[];
  deliveryType: string[];
}