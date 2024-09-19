import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getProduct(productId: string): Promise<ProductModel> {
  const res = await axiosInstance.get('/products/' + productId, {
    responseType: 'json',
  });
  return res.data;
}
