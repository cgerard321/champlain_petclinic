import axiosInstance from '@/shared/api/axiosInstance';
import { InventoryName } from '@/features/inventories/models/InventoryName.ts';
import { SupplyModel } from '@/features/inventories/models/ProductModels/SupplyModel.ts';

export async function addSupplyToInventoryByName(
  inventoryName: string,
  supply: SupplyModel
): Promise<InventoryName[]> {
  const response = await axiosInstance.post(
    `http://localhost:8080/api/v2/gateway/inventories/${inventoryName}/supplies`,
    supply
  );
  return response.data;
}
