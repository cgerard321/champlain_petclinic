import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function updateProduct(
  productId: string,
  productData: Partial<ProductModel>
): Promise<ProductModel> {
  const res = await axiosInstance.put<ProductModel>(
    `/products/${productId}`,
    productData,
    {
      responseType: 'json',
    }
  );
  if (res.status === 200) {
    return res.data;
  } else {
    throw new Error(`Failed to update product: ${res.statusText}`);
  }
}
