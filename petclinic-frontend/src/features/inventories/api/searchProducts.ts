import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';

export async function searchProducts(
  inventoryId: string,
  productName?: string,
  productDescription?: string,
  status?: Status
): Promise<ProductModel[]> {
  const queryParams = new URLSearchParams();
  if (productName) queryParams.append('productName', productName);
  if (productDescription)
    queryParams.append('productDescription', productDescription);
  if (status) queryParams.append('status', status);

  const queryString = queryParams.toString();
  const url = queryString
    ? `products/search?${queryString}`
    : `products/search`;

  const response = await axiosInstance.get<ProductModel[]>(
    `http://localhost:8080/api/v2/gateway/inventories/${inventoryId}/` + url
  );
  return response.data;
}
