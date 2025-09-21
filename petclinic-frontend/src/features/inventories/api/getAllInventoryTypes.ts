import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';

export async function getAllInventoryTypes(): Promise<InventoryType[]> {
  try {
    const response = await axiosInstance.get<InventoryType[]>(
      '/inventory/types',
      {
        useV2: false,
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error getting all Inventory Type:', error);
    throw error;
  }
}
