import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel.ts';

export const addSupplyToInventory = async (
  inventoryId: string,
  product: ProductRequestModel
): Promise<void> => {
  try {
    await axiosInstance.post<void>(
      `/inventories/${inventoryId}/products`,
      product,
      { useV2: false }
    );
  } catch (error) {
    console.error('Error adding supply to Inventory:', error);
    throw error;
  }
};
