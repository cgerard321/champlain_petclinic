import axios from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';

export async function getProductByProductId(productId: string): Promise<ProductModel> {
  try {
    // First, perform the PATCH request to update the product
    await axios.patch(`/products/${productId}`, {}, {
      headers: {
        'Content-Type': 'application/json',
      }
    });

    // Then, perform the GET request to fetch the updated product
    const response = await axios.get<ProductModel>(`/products/${productId}`);
   
    return response.data;
  } catch (error) {
    throw error;
  }
}
