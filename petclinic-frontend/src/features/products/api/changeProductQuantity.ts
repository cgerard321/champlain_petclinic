import axiosInstance from '@/shared/api/axiosInstance';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function changeProductQuantity(productId: string, newQuantity: number): Promise<ProductModel> {///maybe switch to just a product model with just productQuantity. 
  try {
    
    const res = await axiosInstance.patch(`/products/${productId}/quantity`, {
      quantity: newQuantity,
    });

    return res.data;
  } catch (err) {
    console.error(`Error updating quantity for product ${productId}:`, err);
    throw err;
  }
}