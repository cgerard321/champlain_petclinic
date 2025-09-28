import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export default async function addInventoryType(
  inventoryType: Omit<InventoryType, 'typeId'>
): Promise<void> {
  try {
    await axiosInstance.post<void>('/inventories/type', inventoryType, {
      useV2: false,
    });
  } catch (error) {
    console.error('Error adding inventory type:', error);
    throw error; // Re-throw the error after logging
  }
}
