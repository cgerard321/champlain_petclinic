import axiosInstance from '@/shared/api/axiosInstance.ts';

export const fetchCartIdByCustomerId = async (userId: string): Promise<string | null> => {
    try {
        const response = await axiosInstance.get(`http://localhost:8080/api/v2/gateway/carts/customer/${userId}`);
        return response.data.cartId; // Adjust according to your API response structure
    } catch (error) {
        console.error('Error fetching cart ID:', error);
        return null;
    }
};
