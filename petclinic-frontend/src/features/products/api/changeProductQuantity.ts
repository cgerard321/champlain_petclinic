import axiosInstance from '@/shared/api/axiosInstance';


export async function changeProductQuantity(productId: string, newQuantity: number): Promise<void> {
  try {
    await axiosInstance.patch(`/products/${productId}/quantity`, {
      productQuantity: newQuantity,
    });
    // The response is empty, so we don't return anything
  } catch (err) {
    console.error(`Error updating quantity for product ${productId}:`, err);
    throw err;
  }
}