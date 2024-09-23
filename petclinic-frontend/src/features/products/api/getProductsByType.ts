import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getProductsByType(
  productType: string
): Promise<ProductModel[]> {
  const res = await axiosInstance.get(`/products/filter/${productType}`);
  return res.data;
}
