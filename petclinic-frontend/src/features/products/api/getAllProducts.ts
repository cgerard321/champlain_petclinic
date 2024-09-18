import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getAllProducts(): Promise<ProductModel[]> {
  try {
    const res = await axiosInstance.get<ProductModel[]>('/products');
    return res.data;
  } catch (err) {
    console.error('Error fetching products:', err);
    return [];
  }
}
