import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function addProduct(
  product: Omit<ProductModel, 'productId'>
): Promise<ProductModel> {
  try {
    const response = await axiosInstance.post('/products', product, {
      responseType: 'json',
    });

    // eslint-disable-next-line no-console
    console.log(response.data);
    return response.data;
  } catch (error) {
    console.error('Error adding product:', error);
    throw error;
  }
}
