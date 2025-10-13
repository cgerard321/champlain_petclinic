import { Inventory } from '@/features/inventories/models/Inventory';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import axios from 'axios';

export default async function addInventory(
  inventoryData: Omit<Inventory, 'inventoryId'> // Renamed to avoid confusion
): Promise<void> {
  try {
    // Append the appropriate endpoint for adding an inventory
    await axiosInstance.post<void>('/inventories', inventoryData, {
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

    const serverMessage = typeof data?.message === 'string' ? data.message : '';

    switch (status) {
      case 400: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Invalid inventory data. Please review your input and try again.'
        );
      }
      case 404: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Inventory resource was not found.'
        );
      }
      case 422: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Inventory with the same name already exists.'
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
