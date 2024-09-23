import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryName } from '@/features/inventories/models/InventoryName.ts';

export async function getAllInventoryNames(): Promise<InventoryName[]> {
  const response = await axiosInstance.get<InventoryName[]>(
    axiosInstance.defaults.baseURL + 'inventories/names'
  );
  return response.data;
}
