import axiosInstance from '@/shared/api/axiosInstance';
export type CartIdResponse = { cartId: string };
export type CartCountResponse = { itemCount: number };

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

export async function fetchCartCountByCartId(cartId: string): Promise<number> {
  try {
    const { data } = await axiosInstance.get<CartCountResponse>(
      `/carts/${cartId}/count`,
      { useV2: false }
    );
    const n = Number(data?.itemCount);
    return Number.isFinite(n) && n >= 0 ? Math.trunc(n) : 0;
  } catch (error) {
    console.error(`Error fetching cart count for ${cartId}`, error);
    return 0;
  }
}
