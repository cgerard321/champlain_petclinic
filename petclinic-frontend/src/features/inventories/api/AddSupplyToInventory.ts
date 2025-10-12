import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel.ts';
import axios from 'axios';

export const addSupplyToInventory = async (
  inventoryId: string,
  product: ProductRequestModel
): Promise<void> => {
  try {
    await axiosInstance.post<void>(
      `/inventories/${inventoryId}/products`,
      product,
      { useV2: false }
    );
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    const status = error.response?.status ?? 0;
    const data =
      error.response?.data && typeof error.response?.data === 'object'
        ? (error.response?.data as { message?: string })
        : {};
    const serverMessage =
      typeof data.message === 'string' ? data.message.trim() : '';

    switch (status) {
      case 400:
        throw new Error(
          serverMessage ||
            'Invalid product data. Please review your input and try again.'
        );
      case 404:
        throw new Error(serverMessage || 'Inventory not found.');
      case 422:
        throw new Error(
          serverMessage ||
            'Product with the same name already exists in this inventory.'
        );
      case 429:
        throw new Error(
          serverMessage || 'Too many requests. Please try again later.'
        );
      default:
        throw error;
    }
  }
};
