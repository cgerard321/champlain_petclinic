import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryName } from '@/features/inventories/models/InventoryName.ts';

export async function getAllInventoryNames(): Promise<InventoryName[]> {
  try {
    const response = await axiosInstance.get<InventoryName[]>(
      '/inventory/names',
      { useV2: false } // not implemented
    );
    return response.data;
  } catch (error) {
    console.error('Error get all inventory by names:', error);
    throw error;
  }
}
