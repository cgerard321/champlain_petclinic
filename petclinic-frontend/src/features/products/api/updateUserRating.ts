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
    // check if the user has already rated the product
    if (doesUserRatingExist.rating == 0) {
      // if not, create a new rating
      response = await axiosInstance.post<RatingModel>(
        '/ratings/' + productId,
        { rating: newRating, review: newReview },
        {
          responseType: 'json',
          useV2: true,
        }
      );

      return response.data;
    } else {
      // if yes, update the existing rating
      response = await axiosInstance.put<RatingModel>(
        '/ratings/' + productId,
        { rating: newRating, review: newReview },
        {
          responseType: 'json',
          useV2: true,
        }
      );

      return response.data;
    }
  } catch (error) {
    // if any error occurs, return an empty response
    console.error('Error updating user rating:', error);
    return emptyResponse;
  }
}
