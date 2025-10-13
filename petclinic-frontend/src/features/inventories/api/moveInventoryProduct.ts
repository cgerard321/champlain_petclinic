import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';

export const getAllInventories = async (): Promise<
  InventoryResponseModel[]
> => {
  try {
    const response = await axiosInstance.get<InventoryResponseModel[]>(
      '/inventories',
      { useV2: false }
    );
    return response.data;
  } catch (error) {
    console.error('Error getting All Inventories:', error);
    throw error;
  }
};

export const updateProductInventoryId = async (
  currentInventoryId: string,
  productId: string,
  newInventoryId: string
): Promise<void> => {
  try {
    await axiosInstance.put<void>(
      `/inventories/${currentInventoryId}/products/${productId}/updateInventoryId/${newInventoryId}`,
      undefined,
      { useV2: false }
    );
  } catch (error) {
    console.error('Error updating Product by Inventory Id:', error);
    throw error;
  }
};
