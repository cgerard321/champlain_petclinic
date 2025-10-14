//write the axios instance to delete all products from a specific inventory
// import { Inventory } from '@/features/inventories/models/Inventory.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export default async function deleteAllProductsFromInventory(inventory: {
  inventoryId: string;
}): Promise<void> {
  try {
    await axiosInstance.delete<void>(
      `/inventories/${inventory.inventoryId}/products`,
      { useV2: false }
    );
  } catch (error) {
    console.error('Error deleting all products fron inventory:', error);
    throw error;
  }
}
//plan to remove
