import { RatingModel } from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function deleteUserRating(productId: string): Promise<number> {
  return axiosInstance
    .delete<RatingModel>('/ratings/' + productId, {
      responseType: 'json',
    })
    .then(() => {
      return 0;
    })
    .catch(err => {
      console.error(`Could not delete rating for ${productId}`, err);
      return 0;
    });
}
