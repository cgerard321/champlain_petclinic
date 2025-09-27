import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryRequestModel } from '@/features/inventories/models/InventoryModels/InventoryRequestModel.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';

export const updateInventory = async (
  inventoryId: string,
  inventory: InventoryRequestModel
): Promise<void> => {
  try {
    await axiosInstance.put<void>(`/inventories/${inventoryId}`, inventory, {
      useV2: false,
    });
  } catch (error) {
    console.error('Error updating inventory:', error);
    throw error;
  }
};

export const getInventory = async (
  inventoryId: string
): Promise<InventoryResponseModel> => {
  try {
    const response = await axiosInstance.get<InventoryResponseModel>(
      `/inventories/${inventoryId}`,
      { useV2: false }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching Inventories:', error);
    throw error;
  }
};
