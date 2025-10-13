import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import axios from 'axios';

export async function getAllInventoryTypes(): Promise<InventoryType[]> {
  try {
    const response = await axiosInstance.get<InventoryType[]>(
      '/inventories/types',
      {
        useV2: false,
      }
    );
    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;

    console.error('[getAllInventoryTypes]', {
      url: (error.config?.baseURL || '') + (error.config?.url || ''),
      method: (error.config?.method || '').toUpperCase(),
      status: error.response?.status,
      dataReceived: error.response?.data,
    });

    const status = error.response?.status ?? 0;
    if (status === 404) {
      return [];
    }

    throw error;
  }
}
