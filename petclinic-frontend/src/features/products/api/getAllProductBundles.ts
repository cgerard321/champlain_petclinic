import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel.ts';

export async function getAllProductBundles(): Promise<ProductBundleModel[]> {
  try {
    const response = await axiosInstance.get('/products/bundles', {
      useV2: false,
    });

    return response.data;
  } catch (error) {
    console.error('Error fetching product bundles:', error);
    throw error;
  }
}
