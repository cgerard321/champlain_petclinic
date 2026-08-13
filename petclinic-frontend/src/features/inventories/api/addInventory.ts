import { Inventory } from '@/features/inventories/models/Inventory';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ApiResponse } from '@/shared/models/ApiResponse.ts';

export default async function addInventory(
  inventoryData: Omit<Inventory, 'inventoryId'> // Renamed to avoid confusion
): Promise<ApiResponse<Inventory>> {
  try {
    // Append the appropriate endpoint for adding an inventory
    const response = await axiosInstance.post<Inventory>(
      '/inventories',
      inventoryData,
      {
        useV2: false,
      }
    );
    return { data: response.data, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to add inventory. Please check your information and try again.';

    return { data: null, errorMessage };
  }
}
