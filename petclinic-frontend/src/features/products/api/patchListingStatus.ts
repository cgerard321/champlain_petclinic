import { ProductModel } from '../models/ProductModels/ProductModel';
import axiosInstance from '@/shared/api/axiosInstance';

export async function patchListingStatus(
  productId: string,
  productRequestModel: Partial<ProductModel>
): Promise<ProductModel> {
  const url = `products/${productId}/status`;
  const response = await axiosInstance.patch<ProductModel>(
    axiosInstance.defaults.baseURL + url,
    //   `products/${productId}/status`,
    productRequestModel,
    {
      responseType: 'json',
    }
  );

  return response.data;
}
