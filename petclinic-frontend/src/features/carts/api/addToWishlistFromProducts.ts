import axiosInstance from '@/shared/api/axiosInstance';
import { fetchCartIdByCustomerId } from './getCart';
import { useUser } from '@/context/UserContext';

type UseAddToWishlistReturnType = {
  addToWishlist: (productId: string, quantity: number) => Promise<boolean>;
};

export function useAddToWishlist(): UseAddToWishlistReturnType {
  const { user } = useUser();

  const fetchUserCart = async (userId: string): Promise<string | null> => {
    try {
      return await fetchCartIdByCustomerId(userId);
    } catch (error) {
      console.error('Error fetching cart ID:', error);
      return null;
    }
  };

  const addToWishlist = async (
    productId: string,
    quantity: number
  ): Promise<boolean> => {
    if (!user?.userId) {
      console.error('User is not authenticated');
      return false;
    }

    try {
      const cartId = await fetchUserCart(user.userId);
      if (!cartId) {
        console.error('Cart not found');
        return false;
      }

      // was http://localhost:8080/api/v2/gateway/carts/${cartId}/products/${productId}/quantity/${quantity}
      await axiosInstance.post(
        `/carts/${cartId}/products/${productId}/quantity/${quantity}`,
        undefined,
        { useV2: false }
      );
      return true;
    } catch (error) {
      console.error('Error adding product to wishlist:', error);
      return false;
    }
  };

  return { addToWishlist };
}
