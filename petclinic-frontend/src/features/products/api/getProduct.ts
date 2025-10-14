import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getProduct(productId: string): Promise<ProductModel> {
  try {
    const response = await axiosInstance.get('/products/' + productId, {
      useV2: false,
    });

    return response.data;
  } catch (error) {
    console.error('Error fetching product:', error);
    throw error;
  }
}
