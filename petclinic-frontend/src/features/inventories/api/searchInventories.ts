import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Inventory } from '@/features/inventories/models/Inventory.ts';

export async function searchInventories(
    currentPage: number,
    listSize: number,
    inventoryName?: string,
    inventoryType?: string,
    inventoryDescription?: string,
    importantOnly?: boolean
): Promise<Inventory[]> {
  try {
    const queryParams = new URLSearchParams();
    if (inventoryName) queryParams.append('inventoryName', inventoryName);
    if (inventoryType) queryParams.append('inventoryType', inventoryType);
    if (inventoryDescription)
      queryParams.append('inventoryDescription', inventoryDescription);
    if (importantOnly) queryParams.append('importantOnly', 'true');

    const queryString = queryParams.toString();
    const url = queryString
      ? `/inventory?page=${currentPage}&size=${listSize}&${queryString}`
      : `/inventory?page=${currentPage}&size=${listSize}`;

    const response = await axiosInstance.get<Inventory[]>(url, {
      useV2: false,
    });
    return response.data;
  } catch (error) {
    console.error('Error Searching Inventories:', error);
    throw error;
  }
}
