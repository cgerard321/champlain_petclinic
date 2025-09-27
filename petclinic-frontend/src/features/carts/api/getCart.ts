import axiosInstance from '@/shared/api/axiosInstance.ts';

type CartIdResponse = { cartId: string };

export const fetchCartIdByCustomerId = async (
  userId: string
): Promise<string | null> => {
  try {
    // was http://localhost:8080/api/v2/gateway/carts/customer/${userId}
    const { data } = await axiosInstance.get<CartIdResponse>(
      `/carts/customer/${userId}`,
      { useV2: true }
    );
    return data.cartId;
  } catch (error) {
    console.error('Error fetching cart ID:', error);
    return null;
  }
};
