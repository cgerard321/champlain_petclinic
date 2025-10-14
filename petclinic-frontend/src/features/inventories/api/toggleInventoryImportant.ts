import axiosInstance from '@/shared/api/axiosInstance.ts';
import axios from 'axios';

export const toggleInventoryImportant = async (
  inventoryId: string,
  isImportant: boolean
): Promise<void> => {
  try {
    await axiosInstance.patch<void>(
      `/inventories/${inventoryId}/important`,
      { important: isImportant },
      { useV2: false }
    );
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    console.error('[toogleInventoryImportant]', {
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
            : 'Invalid product data. Please review your input and try again.'
        );
      }
      case 404: {
        throw new Error(
          serverMessage.trim()
            ? serverMessage
            : 'Inventory resource was not found.'
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
};
