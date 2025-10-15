import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export default async function addInventoryType(
  inventoryType: Omit<InventoryType, 'typeId'>
): Promise<ApiResponse<InventoryType>> {
  try {
    const response = await axiosInstance.post<InventoryType>(
      '/inventories/type',
      inventoryType,
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
        : 'Unable to add inventory type. Please check your information and try again.';

    return { data: null, errorMessage };
  }
}
