import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductResponseModel } from '@/features/inventories/models/InventoryModels/ProductResponseModel.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel.ts';

export const updateProductInInventory = async (
  inventoryId: string,
  productId: string,
  product: ProductRequestModel
): Promise<void> => {
  try {
    await axiosInstance.put<void>(
      `/inventory/${inventoryId}/products/${productId}`,
      product,
      { useV2: false }
    );
  } catch (error) {
    console.error('Error update product in Inventory:', error);
    throw error;
  }
};

export const getProductByProductIdInInventory = async (
  inventoryId: string,
  productId: string
): Promise<ProductResponseModel> => {
  try {
    const response = await axiosInstance.get<ProductResponseModel>(
      `/inventory/${inventoryId}/products/${productId}`,
      { useV2: false }
    );
    return response.data;
  } catch (error) {
    console.error('Error get product by product id in Inventory:', error);
    throw error;
  }
};
