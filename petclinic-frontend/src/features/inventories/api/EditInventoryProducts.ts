import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductResponseModel } from '@/features/inventories/models/InventoryModels/ProductResponseModel.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel.ts';
import axios from 'axios';

export const updateProductInInventory = async (
  inventoryId: string,
  productId: string,
  product: ProductRequestModel
): Promise<void> => {
  try {
    await axiosInstance.put<void>(
      `/inventories/${inventoryId}/products/${productId}`,
      product,
      { useV2: false }
    );
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    const status = error.response?.status ?? 0;
    const payload: unknown = error.response?.data;

    const data =
      payload && typeof payload === 'object'
        ? (payload as Record<string, unknown>)
        : undefined;

    const serverMessage =
      typeof data?.message === 'string' ? data.message.trim() : '';

    switch (status) {
      case 400:
        throw new Error(
          serverMessage || 'Invalid product data. Please review your input.'
        );
      case 404:
        throw new Error(serverMessage || 'Inventory or product not found.');
      case 422:
        throw new Error(
          serverMessage ||
            'A product with this name already exists in this inventory.'
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

export const getProductByProductIdInInventory = async (
  inventoryId: string,
  productId: string
): Promise<ProductResponseModel> => {
  try {
    const response = await axiosInstance.get<ProductResponseModel>(
      `/inventories/${inventoryId}/products/${productId}`,
      { useV2: false }
    );
    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    const status = error.response?.status ?? 0;
    const payload: unknown = error.response?.data;

    const data =
      payload && typeof payload === 'object'
        ? (payload as Record<string, unknown>)
        : undefined;

    const serverMessage =
      typeof data?.message === 'string' ? data.message.trim() : '';

    switch (status) {
      case 400:
        throw new Error(serverMessage || 'Invalid request.');
      case 404:
        throw new Error(
          serverMessage || 'Product not found in this inventory.'
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
