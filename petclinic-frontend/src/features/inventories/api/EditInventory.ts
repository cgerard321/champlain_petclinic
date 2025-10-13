import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryRequestModel } from '@/features/inventories/models/InventoryModels/InventoryRequestModel.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';
import axios from 'axios';

export const updateInventory = async (
  inventoryId: string,
  inventory: InventoryRequestModel
): Promise<void> => {
  try {
    await axiosInstance.put<void>(`/inventories/${inventoryId}`, inventory, {
      useV2: false,
    });
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
          serverMessage ||
            'Invalid inventory data. Please review your input and try again.'
        );
      case 404:
        throw new Error(serverMessage || 'Inventory not found.');
      case 422:
        throw new Error(
          serverMessage || 'Inventory with the same name already exists.'
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

export const getInventory = async (
  inventoryId: string
): Promise<InventoryResponseModel> => {
  try {
    const response = await axiosInstance.get<InventoryResponseModel>(
      `/inventories/${inventoryId}`,
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
        throw new Error(serverMessage || 'Invalid inventory id.');
      case 404:
        throw new Error(serverMessage || 'Inventory not found.');
      case 429:
        throw new Error(
          serverMessage || 'Too many requests. Please try again later.'
        );
      default:
        throw error;
    }
  }
};
