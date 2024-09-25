import { Inventory } from '@/features/inventories/models/Inventory.ts';
import axiosInstance from '@/shared/api/axiosInstance';

export default async function addInventory(
  inventoryType: Omit<Inventory, 'typeId'>
): Promise<void> {
  try {
    await axiosInstance.post<void>(
      axiosInstance.defaults.baseURL + 'inventories',
      inventoryType
    );
  } catch (error) {
    console.error('Error adding inventory type:', error);
    throw error; // Re-throw the error after logging
  }
}
