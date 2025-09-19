import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';

export async function getAllInventoryTypes(): Promise<InventoryType[]> {
  const response = await axiosInstance.get<InventoryType[]>(
    '/inventory/types',
    {
      useV2: false,
    }
  );
  return response.data;
}
