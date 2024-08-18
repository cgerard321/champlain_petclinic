import { Inventory } from '@/features/inventories/models/Inventory.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export default async function deleteInventory(
  inventory: Inventory
): Promise<void> {
  await axiosInstance.delete<void>(
    axiosInstance.defaults.baseURL + `inventories/${inventory.inventoryId}`
  );
}
