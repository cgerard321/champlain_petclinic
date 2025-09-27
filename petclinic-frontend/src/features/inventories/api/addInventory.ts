import { Inventory } from '@/features/inventories/models/Inventory';
import axiosInstance from '@/shared/api/axiosInstance';

export default async function addInventory(
  inventoryData: Omit<Inventory, 'inventoryId'> // Renamed to avoid confusion
): Promise<void> {
  try {
    // Append the appropriate endpoint for adding an inventory
    await axiosInstance.post<void>('/inventories', inventoryData, {
      useV2: false,
    });
  } catch (error) {
    console.error('Error adding inventory:', error);
    throw error;
  }
}
