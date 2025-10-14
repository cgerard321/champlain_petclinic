import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';

export async function patchGetProduct(
  productId: string
): Promise<ProductModel> {
  try {
    const response = await axiosInstance.patch<ProductModel>(
      `/products/${productId}`,
      {},
      {
        useV2: false,
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    return response.data;
  } catch (error) {
    console.error('Error patching product:', error);
    throw error;
  }
}

// This page needs to be deleted since react is only for customers and not employees.
