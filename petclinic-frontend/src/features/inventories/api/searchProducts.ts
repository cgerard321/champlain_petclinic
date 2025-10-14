import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';
import axios from 'axios';

export async function searchProducts(
  inventoryId: string,
  productName?: string,
  productDescription?: string,
  status?: Status
): Promise<ProductModel[]> {
  try {
    const queryParams = new URLSearchParams();
    if (productName) queryParams.append('productName', productName);
    if (productDescription)
      queryParams.append('productDescription', productDescription);
    if (status) queryParams.append('status', status);

    const queryString = queryParams.toString();
    const url = queryString
      ? `/inventories/${inventoryId}/products/search?${queryString}`
      : `/inventories/${inventoryId}/products/search`;

    const response = await axiosInstance.get<ProductModel[]>(url, {
      useV2: false,
    });
    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    const status = error.response?.status ?? 0;
    const payload: unknown = error.response?.data;

    const data =
      payload && typeof payload === 'object'
        ? (payload as Record<string, unknown>)
        : undefined;

    const serverMessage = typeof data?.message === 'string' ? data.message : '';

    switch (status) {
      case 400: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Invalid product data. Please review your input and try again.'
        );
      }
      case 404: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Product resource was not found.'
        );
      }
      case 429: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Too many requests. Please try again later.'
        );
      }
      default:
        throw error;
    }
  }
}
