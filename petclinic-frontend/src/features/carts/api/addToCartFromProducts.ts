import axiosInstance from '@/shared/api/axiosInstance';
import { AxiosError } from 'axios';
import { fetchCartIdByCustomerId } from './getCart';
import { useUser } from '@/context/UserContext';

type UseAddToCartReturnType = {
  addToCart: (productId: string) => Promise<boolean>;
};

type CreateCartResponse = {
  cartId?: string;
  id?: string;
  [k: string]: unknown;
};

export function useAddToCart(): UseAddToCartReturnType {
  const { user } = useUser();

  const getOrCreateCartId = async (userId: string): Promise<string> => {
    try {
      // GET /carts/customer/{customerId} (via ton helper)
      const existingCartId = await fetchCartIdByCustomerId(userId);
      if (!existingCartId) throw new Error('Cart not found');
      return existingCartId;
    } catch (err) {
      const ax = err as AxiosError;
      const status = ax.response?.status;

      if (status === 404 || status === 401) {
        const { data } = await axiosInstance.post<CreateCartResponse>(
          '/carts', // → POST /api/v2/gateway/carts
          { customerId: userId },
          { headers: { 'Content-Type': 'application/json' } }
        );

        const newId = (data?.cartId ?? data?.id) as string | undefined;
        if (!newId) throw new Error('Could not create cart');
        return newId;
      }

      throw err;
    }
  };

  const addToCart = async (productId: string): Promise<boolean> => {
    if (!user?.userId) {
      alert('You must be logged in.');
      return false;
    }

    try {
      const cartId = await getOrCreateCartId(user.userId);

      await axiosInstance.post(
        `/carts/${encodeURIComponent(cartId)}/${encodeURIComponent(String(productId))}`
      );

      return true;
    } catch (err) {
      const ax = err as AxiosError;
      const status = ax.response?.status ?? 'unknown';
      const payload = ax.response?.data ?? ax.message;

      console.error('AddToCart failed:', status, payload);
      alert(`Add to cart failed (${status}).`);
      return false;
    }
  };

  return { addToCart };
}
