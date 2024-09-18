import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryRequestModel } from '@/features/customers/models/InventoryRequestModel.ts';
import { InventoryResponseModel } from '@/features/customers/models/InventoryResponseModel.ts';

export const updateInventory = async (
  inventoryId: string,
  inventory: InventoryRequestModel
): Promise<void> => {
  await axiosInstance.put<void>(`inventories/${inventoryId}`, inventory);
};

export const getInventory = async (
  inventoryId: string
): Promise<InventoryResponseModel> => {
  const response = await axiosInstance.get<InventoryResponseModel>(
    `http://localhost:8080/api/v2/gateway/inventories/${inventoryId}`
  );
  return response.data;
};
