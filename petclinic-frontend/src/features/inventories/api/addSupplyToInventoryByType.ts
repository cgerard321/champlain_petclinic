import axiosInstance from '@/shared/api/axiosInstance';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import { SupplyModel } from '@/features/inventories/models/ProductModels/SupplyModel.ts';

export async function addSupplyToInventoryByType(
  inventoryType: string,
  supply: SupplyModel
): Promise<InventoryType[]> {
  const response = await axiosInstance.post(
    `http://localhost:8080/api/v2/gateway/inventories/${inventoryType}/supplies`,
    supply
  );
  return response.data;
}
