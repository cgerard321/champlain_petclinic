import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel.ts';

export async function getAllProductBundles(): Promise<ProductBundleModel[]> {
  const res = await axiosInstance.get('/products/bundles');
  return res.data;
}
