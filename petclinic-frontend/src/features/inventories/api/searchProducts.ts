import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export async function searchProducts(
  inventoryId: string,
  productName?: string,
  productDescription?: string,
  status?: Status
): Promise<ApiResponse<ProductModel[]>> {
  try {
    const queryParams = new URLSearchParams();
    if (productName) queryParams.append('productName', productName);
    if (productDescription)
      queryParams.append('productDescription', productDescription);
    if (status) queryParams.append('status', status);

    const queryString = queryParams.toString();
    const url = queryString
      ? `/inventories/${inventoryId}/products/search?${queryString}`
      : `/inventories/${inventoryId}/products/search`;

    const res = await axiosInstance.get<ProductModel[]>(url, {
      useV2: false,
    });

    return { data: res.data, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      ?.response?.data?.message;
    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Failed to load products. Please try again.';
    return { data: null, errorMessage };
  }
}
