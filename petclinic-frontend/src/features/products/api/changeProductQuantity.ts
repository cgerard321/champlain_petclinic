import axiosInstance from '@/shared/api/axiosInstance';

export async function changeProductQuantity(
  productId: string,
  newQuantity: number
): Promise<void> {
  try {
    await axiosInstance.patch(`/products/${productId}/quantity`, {
      productQuantity: newQuantity,
      useV2: false,
    });
  } catch (err) {
    console.error(`Error updating quantity for product ${productId}:`, err);
    throw err;
  }
}

// This page needs to be deleted since react is only for customers and not employees.
