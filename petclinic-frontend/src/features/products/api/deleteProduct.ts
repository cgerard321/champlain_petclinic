import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export const deleteProduct = async (
  productId: string
): Promise<AxiosResponse<ProductModel>> => {
  return await axiosInstance.delete<ProductModel>(`/products/${productId}`);
};
