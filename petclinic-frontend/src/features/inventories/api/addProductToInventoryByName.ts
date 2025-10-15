import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryName } from '@/features/inventories/models/InventoryName.ts';
import { ProductModelINVT } from '@/features/inventories/models/ProductModels/ProductModelINVT.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export async function addProductToInventoryByName(
  inventoryName: string,
  product: ProductModelINVT
): Promise<ApiResponse<InventoryName[]>> {
  //Not implemented neither in v1 or v2
  try {
    const response = await axiosInstance.post(
      `/inventories/${inventoryName}/products/by-name`,
      product,
      { useV2: false }
    );
    return { data: response.data, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to add product to inventory. Please check your information and try again.';

    return { data: null, errorMessage };
  }
}
