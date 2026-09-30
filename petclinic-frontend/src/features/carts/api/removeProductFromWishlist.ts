import axiosInstance from '@/shared/api/axiosInstance';
import { fetchCartIdByCustomerId } from './getCart';
import { useUser } from '@/context/UserContext';

type UseRemoveFromWishlistReturnType = {
  removeFromWishlistByIcon: (productId: string) => Promise<boolean>;
};

export function useRemoveFromWishlistByIcon(): UseRemoveFromWishlistReturnType {
  const { user } = useUser();

  const fetchUserCart = async (userId: string): Promise<string | null> => {
    try {
      return await fetchCartIdByCustomerId(userId);
    } catch (error) {
      console.error('Error fetching cart ID:', error);
      return null;
    }
  };

  const removeFromWishlistByIcon = async (
    productId: string
  ): Promise<boolean> => {
    if (!user?.userId) {
      console.error('User is not authenticated');
      return false;
    }

    const normalizedProductId = productId?.trim();
    if (!normalizedProductId) {
      console.error('Invalid product identifier');
      return false;
    }

    try {
      const cartId = await fetchUserCart(user.userId);
      if (!cartId) {
        console.error('Cart not found');
        return false;
      }

      await axiosInstance.delete(
        `/carts/${encodeURIComponent(cartId)}/wishlist`
      );
      return true;
    } catch (error) {
      console.error('Error removing product to wishlist:', error);
      return false;
    }
  };

  return { removeFromWishlistByIcon };
}
