import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductResponseModel } from '@/features/inventories/models/InventoryModels/ProductResponseModel.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel.ts';

export const updateProductInInventory = async (
  inventoryId: string,
  productId: string,
  product: ProductRequestModel
): Promise<void> => {
  await axiosInstance.put<void>(
    `inventories/${inventoryId}/products/${productId}`,
    product
  );
};

export const getProductByProductIdInInventory = async (
  inventoryId: string,
  productId: string
): Promise<ProductResponseModel> => {
  const response = await axiosInstance.get<ProductResponseModel>(
    `http://localhost:8080/api/v2/gateway/inventories/${inventoryId}/products/${productId}`
  );
  return response.data;
};
