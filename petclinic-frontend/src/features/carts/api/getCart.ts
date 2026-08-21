import axiosInstance from '@/shared/api/axiosInstance';
import type { CartDetailsModel } from '@/shared/api/cart';

export type CartIdResponse = { cartId: string };

export async function fetchCartIdByCustomerId(
  userId: string
): Promise<string | null> {
  try {
    // RESTful location: http://localhost:8080/api/gateway/customers/{customerId}/cart
    const { data } = await axiosInstance.get<CartIdResponse>(
      `/customers/${userId}/cart`,
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

export function calculateCartItemsCount(cart: CartDetailsModel | null): number {
  if (!cart || !Array.isArray(cart.products)) return 0;

  const totalByQuantity = cart.products.reduce((acc, product) => {
    const quantity = Number(product?.quantityInCart);
    if (Number.isFinite(quantity) && quantity > 0) {
      return acc + Math.trunc(quantity);
    }
    return acc + 1;
  }, 0);

  return totalByQuantity > 0 ? totalByQuantity : cart.products.length;
}

export async function fetchCartCountByCartId(cartId: string): Promise<number> {
  const cart = await fetchCartDetailsByCartId(cartId);
  return calculateCartItemsCount(cart);
}
