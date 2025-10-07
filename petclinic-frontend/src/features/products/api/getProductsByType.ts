import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel.ts';

export async function getProductsByType(
  productType: string
): Promise<ProductModel[]> {
  try {
    const response = await axiosInstance.get(
      '/products/filter/' + productType,
      {
        useV2: false,
      }
    );

    return response.data;
  } catch (error) {
    console.error('Error fetching products by type:', error);
    throw error;
  }
}
