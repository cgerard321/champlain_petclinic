import { Inventory } from '@/features/inventories/models/Inventory.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import axios from 'axios';

export default async function deleteInventory(
  inventory: Inventory
): Promise<void> {
  try {
    await axiosInstance.delete<void>(`/inventories/${inventory.inventoryId}`, {
      useV2: false,
    });
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    console.error('[deleteInventory]', {
      url: (error.config?.baseURL || '') + (error.config?.url || ''),
      method: (error.config?.method || '').toUpperCase(),
      status: error.response?.status,
      dataReceived: error.response?.data,
    });

    const status = error.response?.status ?? 0;
    const body = (error.response?.data ?? {}) as Record<string, unknown>;
    const serverMessage = typeof body.message === 'string' ? body.message : '';

    switch (status) {
      case 400:
        throw new Error(serverMessage || 'Invalid inventory id.');
      case 404:
        throw new Error(
          serverMessage || 'Inventory not found or already deleted.'
        );
      case 429:
        throw new Error(
          serverMessage || 'Too many requests. Please try again later.'
        );
      default:
        throw new Error(serverMessage || 'Failed to delete inventory.');
    }
  }
}
