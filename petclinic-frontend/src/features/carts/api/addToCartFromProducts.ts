import axiosInstance from '@/shared/api/axiosInstance';
import { fetchCartIdByCustomerId } from './getCart';
import { useUser } from '@/context/UserContext';
import { notifyCartChanged } from './cartEvent';

type UseAddToCartReturnType = {
  addToCart: (productId: string) => Promise<boolean>;
};

export function useAddToCart(): UseAddToCartReturnType {
  const { user } = useUser();

  const fetchUserCart = async (userId: string): Promise<string | null> => {
    try {
      return await fetchCartIdByCustomerId(userId);
    } catch (error) {
      console.error('Error fetching cart ID:', error);
      return null;
    }
  };

  const addToCart = async (productId: string): Promise<boolean> => {
    if (!user?.userId) {
      console.error('User is not authenticated');
      return false;
    }

    try {
      const cartId = await fetchUserCart(user.userId);
      if (!cartId) {
        throw new Error('Cart not found');
      }

      const endpoint = `http://localhost:8080/api/v2/gateway/carts/${cartId}/${productId}`;
      await axiosInstance.post(endpoint);

      notifyCartChanged();

      return true; //returns true
    } catch (error) {
      console.error('Error adding product to cart:', error);
      return false; //returns false
    }
  };

  return { addToCart };
}
