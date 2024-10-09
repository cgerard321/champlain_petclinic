//write the axios instance to delete all products from a specific inventory
// import { Inventory } from '@/features/inventories/models/Inventory.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export default async function deleteAllProductsFromInventory(inventory: {
  inventoryId: string;
}): Promise<void> {
  await axiosInstance.delete<void>(
    axiosInstance.defaults.baseURL +
      `inventories/${inventory.inventoryId}/products`
  );
}
