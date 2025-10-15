import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductResponseModel } from '@/features/inventories/models/InventoryModels/ProductResponseModel.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export const updateProductInInventory = async (
  inventoryId: string,
  productId: string,
  product: ProductRequestModel
): Promise<ApiResponse<void>> => {
  try {
    await axiosInstance.put<void>(
      `/inventories/${inventoryId}/products/${productId}`,
      product,
      { useV2: false }
    );
    return { data: undefined, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to update inventory produdct. Please check your information and try again.';

    return { data: null, errorMessage };
  }
};

export const getProductByProductIdInInventory = async (
  inventoryId: string,
  productId: string
): Promise<ApiResponse<ProductResponseModel>> => {
  try {
    const response = await axiosInstance.get<ProductResponseModel>(
      `/inventories/${inventoryId}/products/${productId}`,
      { useV2: false }
    );
    return { data: response.data, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to get product by productID in inventory. Please check your information and try again.';

    return { data: null, errorMessage };
  }
};
