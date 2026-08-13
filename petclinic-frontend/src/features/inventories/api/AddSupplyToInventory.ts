import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export const addSupplyToInventory = async (
  inventoryId: string,
  product: ProductRequestModel
): Promise<ApiResponse<ProductRequestModel>> => {
  try {
    await axiosInstance.post<ProductRequestModel>(
      `/inventories/${inventoryId}/products`,
      product,
      { useV2: false }
    );
    return { data: product, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to add supply to inventory. Please check your information and try again.';

    return { data: null, errorMessage };
  }
};
