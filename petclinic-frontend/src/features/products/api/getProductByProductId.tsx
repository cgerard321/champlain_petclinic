import axios from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getProductByProductId(
  productId: string
): Promise<ProductModel> {
  try {
    await axios.patch(
      `/products/${productId}`,
      {},
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    const response = await axios.get<ProductModel>(`/products/${productId}`);

    return response.data;
  } catch (error) {
    throw error;
  }
}
