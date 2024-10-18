import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';

export const getAllInventories = async (): Promise<
  InventoryResponseModel[]
> => {
  const response = await axiosInstance.get<InventoryResponseModel[]>(
    'http://localhost:8080/api/v2/gateway/inventories/all'
  );
  return response.data;
};

export const updateProductInventoryId = async (
  currentInventoryId: string,
  productId: string,
  newInventoryId: string
): Promise<void> => {
  await axiosInstance.put<void>(
    `http://localhost:8080/api/v2/gateway/inventories/${currentInventoryId}/products/${productId}/updateInventoryId/${newInventoryId}`
  );
};
