import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';

export async function patchGetProduct(productId: string): Promise<ProductModel> {
  try {
  
    const response = await axiosInstance.patch<ProductModel>(`/products/${productId}`);
    
  
    return response.data;
  } catch (error) {
    console.error('Error fetching product by ID:', error);
    throw error; 
  }
}