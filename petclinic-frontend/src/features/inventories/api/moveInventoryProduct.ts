import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';
import { ApiResponse } from '@/shared/models/ApiResponse.ts';

export const getAllInventories = async (): Promise<
  ApiResponse<InventoryResponseModel[]>
> => {
  try {
    const response = await axiosInstance.get<InventoryResponseModel[]>(
      '/inventories',
      { useV2: false }
    );
    return { data: response.data, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to fetch inventories. Please try again.';

    return { data: null, errorMessage };
  }
};

export const updateProductInventoryId = async (
  currentInventoryId: string,
  productId: string,
  newInventoryId: string
): Promise<ApiResponse<void>> => {
  try {
    await axiosInstance.put<void>(
      `/inventories/${currentInventoryId}/products/${productId}/updateInventoryId/${newInventoryId}`,
      undefined,
      { useV2: false }
    );
    return { data: undefined, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to move product to the selected inventory. Please try again.';

    return { data: null, errorMessage };
  }
};
