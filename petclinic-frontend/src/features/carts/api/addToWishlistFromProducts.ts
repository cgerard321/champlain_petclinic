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

    const normalizedProductId = productId?.trim();
    if (!normalizedProductId) {
      console.error('Invalid product identifier');
      return false;
    }

    const coercedQuantity = Number.isFinite(quantity)
      ? Number(quantity)
      : Number.parseInt(String(quantity ?? ''), 10);
    const wishlistQuantity =
      coercedQuantity > 0 ? Math.floor(coercedQuantity) : 1;

    try {
      const cartId = await fetchUserCart(user.userId);
      if (!cartId) {
        console.error('Cart not found');
        return false;
      }

      await axiosInstance.post(
        `/carts/${encodeURIComponent(cartId)}/wishlist`,
        {
          productId: normalizedProductId,
          quantity: wishlistQuantity,
        }
      );
      return true;
    } catch (error) {
      console.error('Error adding product to wishlist:', error);
      return false;
    }
  };

  return { addToWishlist };
}
