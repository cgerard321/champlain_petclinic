import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Inventory } from '@/features/inventories/models/Inventory.ts';

export async function searchInventories(
  currentPage: number,
  listSize: number,
  inventoryName?: string,
  inventoryType?: string,
  inventoryDescription?: string
): Promise<Inventory[]> {
  const queryParams = new URLSearchParams();
  if (inventoryName) queryParams.append('inventoryName', inventoryName);
  if (inventoryType) queryParams.append('inventoryType', inventoryType);
  if (inventoryDescription)
    queryParams.append('inventoryDescription', inventoryDescription);

  const queryString = queryParams.toString();
  const url = queryString
    ? `inventories?page=${currentPage}&size=${listSize}&${queryString}`
    : `inventories?page=${currentPage}&size=${listSize}`;

  const response = await axiosInstance.get<Inventory[]>(
    axiosInstance.defaults.baseURL + 'v2/gateway/' + url
  );
  return response.data;
}
