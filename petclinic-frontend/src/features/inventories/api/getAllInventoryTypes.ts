import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';

export async function getAllInventoryTypes(): Promise<InventoryType[]> {
  const response = await axiosInstance.get<InventoryType[]>(
    axiosInstance.defaults.baseURL + 'v2/gateway/inventories/types'
  );
  return response.data;
}
