import axiosInstance from '@/shared/api/axiosInstance';
import type { CartDetailsModel } from '@/shared/api/cart';

export interface AddProductToCartRequest {
  productId: string;
  quantity: number;
}

export async function addProductToCart(
  cartId: string,
  request: AddProductToCartRequest
): Promise<CartDetailsModel | null> {
  try {
    const response = await axiosInstance.post<CartDetailsModel>(
      `/carts/${encodeURIComponent(cartId)}/products`,
      request,
      { useV2: false }
    );

    return response.data;
  } catch (error) {
    console.error('Error adding product to cart:', error);
    throw error;
  }
}
