import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';
import axios from 'axios';

export const getAllInventories = async (): Promise<
  InventoryResponseModel[]
> => {
  try {
    const response = await axiosInstance.get<InventoryResponseModel[]>(
      '/inventories',
      { useV2: false }
    );
    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    console.error('[getAllInventories]', {
      url: (error.config?.baseURL || '') + (error.config?.url || ''),
      method: (error.config?.method || '').toUpperCase(),
      status: error.response?.status,
      dataReceived: error.response?.data,
    });

    const status = error.response?.status ?? 0;
    const payload: unknown = error.response?.data;

    const data =
      payload && typeof payload === 'object'
        ? (payload as Record<string, unknown>)
        : undefined;

    const serverMessage =
      typeof data?.message === 'string' ? data.message.trim() : '';

    switch (status) {
      case 401:
        throw new Error(serverMessage || 'Not authenticated.');
      case 403:
        throw new Error(serverMessage || 'Not authorized to view inventories.');
      case 404:
        throw new Error(serverMessage || 'Inventories not found.');
      case 429:
        throw new Error(
          serverMessage || 'Too many requests. Please try again later.'
        );
      default:
        throw new Error(serverMessage || 'Failed to load inventories.');
    }
  }
};

export const updateProductInventoryId = async (
  currentInventoryId: string,
  productId: string,
  newInventoryId: string
): Promise<void> => {
  try {
    await axiosInstance.put<void>(
      `/inventories/${currentInventoryId}/products/${productId}/updateInventoryId/${newInventoryId}`,
      undefined,
      { useV2: false }
    );
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    console.error('[updateProductInventoryId]', {
      url: (error.config?.baseURL || '') + (error.config?.url || ''),
      method: (error.config?.method || '').toUpperCase(),
      status: error.response?.status,
      dataReceived: error.response?.data,
    });

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
          serverMessage || 'Invalid move request. Check the IDs you provided.'
        );
      case 404:
        throw new Error(serverMessage || 'Product or inventory not found.');
      case 409:
        throw new Error(
          serverMessage ||
            'Conflict moving product. It may already belong to the target inventory.'
        );
      case 422:
        throw new Error(
          serverMessage || 'Cannot move product due to validation rules.'
        );
      case 429:
        throw new Error(
          serverMessage || 'Too many requests. Please try again later.'
        );
      default:
        throw new Error(serverMessage || 'Failed to move product.');
    }
  }
};
