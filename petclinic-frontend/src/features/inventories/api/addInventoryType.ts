import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import axios from 'axios';

export default async function addInventoryType(
  inventoryType: Omit<InventoryType, 'typeId'>
): Promise<void> {
  try {
    await axiosInstance.post<void>('/inventories/type', inventoryType, {
      useV2: false,
    });
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    console.error('[addInventoryType]', {
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
    const serverMessage = typeof data?.message === 'string' ? data.message : '';

    switch (status) {
      case 400: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Invalid inventory type data. Please review your input and try again.'
        );
      }
      case 422: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Validation failed. An inventory with the same name already exist.'
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
