import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getProductByProductId(
  productId: string
): Promise<ProductModel> {
  try {
    await axiosInstance.patch(
      `/products/${productId}`,
      {},
      {
        headers: {
          'Content-Type': 'application/json',
        },
        useV2: false,
      }
    );

    const response = await axiosInstance.get<ProductModel>(
      `/products/${productId}`,
      {
        useV2: false,
      }
    );

    return response.data;
  } catch (error) {
    throw error;
  }
}
