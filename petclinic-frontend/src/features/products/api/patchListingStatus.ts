import { ProductModel } from '../models/ProductModels/ProductModel';
import axiosInstance from '@/shared/api/axiosInstance';

export async function patchListingStatus(
  productId: string,
  productRequestModel: Partial<ProductModel>
): Promise<ProductModel> {
  try {
    const url = `products/${productId}/status`;
    const response = await axiosInstance.patch<ProductModel>(
      axiosInstance.defaults.baseURL + url,
      //   `products/${productId}/status`,
      productRequestModel,
      {
        responseType: 'json',
        useV2: false,
      }
    );

    return response.data;
  } catch (error) {
    console.error('Error updating product listing status:', error);
    throw error;
  }
}

// This page needs to be deleted since react is only for customers and not employees.
