import { RatingModel } from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { getUserRating } from './getUserRating';

export async function updateUserRating(
  productId: string,
  newRating: number,
  newReview: string | null
): Promise<RatingModel> {
  const emptyResponse = { rating: 0, review: '' };
  try {
    const doesUserRatingExist = await getUserRating(productId);
    let response;
    if (doesUserRatingExist.rating == 0) {
      response = await axiosInstance.post<RatingModel>(
        '/ratings/' + productId,
        { rating: newRating, review: newReview },
        {
          responseType: 'json',
          useV2: false,
        }
      );

      return response.data;
    } else {
      response = await axiosInstance.put<RatingModel>(
        '/ratings/' + productId,
        { rating: newRating, review: newReview },
        {
          responseType: 'json',
          useV2: false,
        }
      );

      return response.data;
    }
  } catch (error) {
    console.error('Error updating user rating:', error);
    return emptyResponse;
  }
}
