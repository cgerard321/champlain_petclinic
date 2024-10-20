import axiosInstance from '@/shared/api/axiosInstance';
import {fetchCartIdByCustomerId} from './getCart';
import {useUser} from '@/context/UserContext';

export class addToCartFromProducts {

    //fetching the user cart with userContext
    static async fetchUserCart(userId: string): Promise<string | null> {
        try {
            return await fetchCartIdByCustomerId(userId);
        } catch (error) {
            console.error('Error fetching cart ID:', error);
            return null;
        }
    }

    //add the product
    static async addToCart(productId: String): Promise<void> {
        const { user } = useUser();

        //check authentication
        if (!user?.userId) {
            throw new Error('User is not authenticated');
        }

        try {
            //fetch the cartId of the current user
            const cartId = await this.fetchUserCart(user.userId);
            if (!cartId) {
                throw new Error('Cart not found');
            }

            //post request
            const endpoint = `/api/cart/${cartId}/add`;
            await axiosInstance.post(endpoint, { productId });

            console.log('Product added to cart:', productId);
        } catch (error) {
            console.error('Error adding product to cart:', error);
        }
    }
}
