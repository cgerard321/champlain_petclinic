import axiosInstance from '@/shared/api/axiosInstance';
import { InventoryName } from '@/features/inventories/models/InventoryName.ts';
import { ProductModelINVT } from '@/features/inventories/models/ProductModels/ProductModelINVT.ts';

export async function addProductToInventoryByName(
  inventoryName: string,
  product: ProductModelINVT
): Promise<InventoryName[]> {
  const response = await axiosInstance.post(
    `http://localhost:8080/api/v2/gateway/inventories/${inventoryName}/products/by-name`,
    product
  );
  return response.data;
}
