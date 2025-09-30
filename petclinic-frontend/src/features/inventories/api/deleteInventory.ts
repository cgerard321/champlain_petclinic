import { Inventory } from '@/features/inventories/models/Inventory.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export default async function deleteInventory(
  inventory: Inventory
): Promise<void> {
  try {
    await axiosInstance.delete<void>(`/inventories/${inventory.inventoryId}`, {
      useV2: false,
    });
  } catch (error) {
    console.error('Error deleting the Inventory:', error);
    throw error;
  }
}
