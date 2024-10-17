import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel.ts';

export async function getProductBundleById(
  bundleId: string
): Promise<ProductBundleModel> {
  const res = await axiosInstance.get(`/products/bundles/${bundleId}`);
  return res.data;
}
