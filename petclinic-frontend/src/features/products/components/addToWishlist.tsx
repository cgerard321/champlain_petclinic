import axios from 'axios';

export async function addToWishlist(
  productId: string,
  cartId: string | null,
  quantity: number
): Promise<void> {
  // Check if cartId is null and return an appropriate message
  if (!cartId) {
    throw new Error('Cart ID is required to add products to the wishlist.');
  }

  try {
    const response = await axios.post(
      `http://localhost:8080/api/v2/gateway/carts/${cartId}/products/${productId}/quantity/${quantity}`
    );

    if (response.status !== 200) {
      throw new Error('Failed to add product to wishlist');
    }
  } catch (error) {
    console.error('Error adding product to wishlist:', error);
    throw error;
  }
}
