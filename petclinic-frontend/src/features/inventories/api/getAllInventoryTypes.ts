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

    const status = error.response?.status ?? 0;
    if (status === 400) {
      throw new Error('No inventory types found.');
    }

    throw error; // Re-throw the error if not handled above
  }
}
