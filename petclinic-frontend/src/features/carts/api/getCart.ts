import axiosInstance from '@/shared/api/axiosInstance';
import type { CartDetailsModel } from '@/shared/api/cart';

export type CartIdResponse = { cartId: string };

export async function fetchCartIdByCustomerId(
  userId: string
): Promise<string | null> {
  try {
    // was http://localhost:8080/api/v2/gateway/carts/customer/${userId}
    const { data } = await axiosInstance.get<CartIdResponse>(
      `/carts/customer/${userId}`,
      { useV2: false }
    );
    return data?.cartId ?? null;
  } catch (error) {
    console.error('Error fetching cart ID:', error);
    return null;
  }
}

export async function fetchCartDetailsByCartId(
  cartId: string
): Promise<CartDetailsModel | null> {
  try {
    const { data } = await axiosInstance.get<CartDetailsModel>(
      `/carts/${cartId}`,
      { useV2: false }
    );
    return data ?? null;
  } catch (error) {
    console.error(`Error fetching cart for ${cartId}`, error);
    return null;
  }
}
