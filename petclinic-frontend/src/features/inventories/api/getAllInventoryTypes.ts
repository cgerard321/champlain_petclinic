import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import { ApiResponse } from '@/shared/models/ApiResponse.ts';

export async function getAllInventoryTypes(): Promise<
  ApiResponse<InventoryType[]>
> {
  try {
    const response = await axiosInstance.get<InventoryType[]>(
      '/inventories/types',
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
