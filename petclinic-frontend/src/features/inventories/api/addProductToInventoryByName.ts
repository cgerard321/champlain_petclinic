import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryName } from '@/features/inventories/models/InventoryName.ts';
import { ProductModelINVT } from '@/features/inventories/models/ProductModels/ProductModelINVT.ts';

export async function addProductToInventoryByName(
  inventoryName: string,
  product: ProductModelINVT
): Promise<InventoryName[]> {
  //Not implemented neither in v1 or v2
  try {
    const response = await axiosInstance.post(
      `/inventories/${inventoryName}/products/by-name`,
      product,
      { useV2: false }
    );
    return response.data;
  } catch (error) {
    console.error('Error adding Product to Inventory:', error);
    throw error;
  }
}
