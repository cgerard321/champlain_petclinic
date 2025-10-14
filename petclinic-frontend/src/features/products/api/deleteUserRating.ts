import { RatingModel } from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function deleteUserRating(productId: string): Promise<number> {
  try {
    return await axiosInstance
      .delete<RatingModel>('/ratings/' + productId, {
        useV2: false,
      })
      .then(() => {
        return 0;
      });
  } catch (error) {
    console.error(`Could not delete rating for ${productId}`, error);
    throw error;
  }
}
