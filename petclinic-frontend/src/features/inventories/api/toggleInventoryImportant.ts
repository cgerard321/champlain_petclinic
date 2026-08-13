import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export const toggleInventoryImportant = async (
  inventoryId: string,
  isImportant: boolean
): Promise<ApiResponse<void>> => {
  try {
    await axiosInstance.patch<void>(
      `/inventories/${inventoryId}/important`,
      { important: isImportant },
      { useV2: false }
    );
    return { data: undefined, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const fallback = 'Unable to update inventory importance. Please try again.';

    const msg =
      typeof maybeMsg === 'string' && maybeMsg.trim() ? maybeMsg.trim() : '';

    if (msg) return { data: null, errorMessage: msg };

    return { data: null, errorMessage: fallback };
  }
};
