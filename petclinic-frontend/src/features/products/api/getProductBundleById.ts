import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel.ts';

export async function getProductBundleById(
  bundleId: string
): Promise<ProductBundleModel> {
  try {
    const response = await axiosInstance.get('/products/bundles/' + bundleId, {
      useV2: false,
    });

    return response.data;
  } catch (error) {
    console.error('Error fetching product bundle:', error);
    throw error;
  }
}
