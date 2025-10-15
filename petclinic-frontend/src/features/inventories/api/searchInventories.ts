import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Inventory } from '@/features/inventories/models/Inventory.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export async function searchInventories(
  currentPage: number,
  listSize: number,
  inventoryName?: string,
  inventoryType?: string,
  inventoryDescription?: string,
  importantOnly?: boolean
): Promise<ApiResponse<Inventory[]>> {
  try {
    const queryParams = new URLSearchParams();
    if (inventoryName) queryParams.append('inventoryName', inventoryName);
    if (inventoryType) queryParams.append('inventoryType', inventoryType);
    if (inventoryDescription)
      queryParams.append('inventoryDescription', inventoryDescription);
    if (importantOnly) queryParams.append('importantOnly', 'true');

    const queryString = queryParams.toString();
    const url = queryString
      ? `/inventories?page=${currentPage}&size=${listSize}&${queryString}`
      : `/inventories?page=${currentPage}&size=${listSize}`;

    const res = await axiosInstance.get<Inventory[]>(url, {
      useV2: false,
    });
    return { data: res.data, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      ?.response?.data?.message;
    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Failed to load inventories. Please try again.';
    return { data: null, errorMessage };
  }
}
