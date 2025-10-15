import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryRequestModel } from '@/features/inventories/models/InventoryModels/InventoryRequestModel.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';
import { ApiResponse } from '@/shared/models/ApiResponse.ts';

export async function updateInventory(
  inventoryId: string,
  inventory: InventoryRequestModel
): Promise<ApiResponse<void>> {
  try {
    await axiosInstance.put<void>(`/inventories/${inventoryId}`, inventory, {
      useV2: false,
    });
    return { data: undefined, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to update inventory. Please check your information and try again.';

    return { data: null, errorMessage };
  }
}

export async function getInventory(
  inventoryId: string
): Promise<ApiResponse<InventoryResponseModel>> {
  try {
    const response = await axiosInstance.get<InventoryResponseModel>(
      `/inventories/${inventoryId}`,
      { useV2: false }
    );
    return { data: response.data, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to fetch inventory. Please check your information and try again.';
    return { data: null, errorMessage };
  }
}
