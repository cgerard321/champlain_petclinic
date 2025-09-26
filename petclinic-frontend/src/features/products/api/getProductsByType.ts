import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel.ts';

export async function getProductsByType(
  productType: string
): Promise<ProductModel[]> {
  const res = await axiosInstance.get(`/products/filter/${productType}`, {
    useV2: true,
  });
  return res.data;
}
