import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function updateProduct(
  productId: string,
  productData: Partial<ProductModel>
): Promise<ProductModel> {
  try {
    const response = await axiosInstance.put<ProductModel>(
      `/products/${productId}`,
      productData,
      {
        responseType: 'json',
        useV2: false,
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error updating product:', error);
    throw error;
  }
}
