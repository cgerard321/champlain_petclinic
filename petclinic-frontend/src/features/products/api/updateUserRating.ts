import {
  RatingCategories,
  RatingModel,
} from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { getUserRating } from './getUserRating';

export async function updateUserRating(
  productId: string,
  newRating: number,
  newReview: string | null,
  categoryRatings: RatingCategories
): Promise<RatingModel> {
  try {
    const doesUserRatingExist = await getUserRating(productId);

    const payload = {
      rating: newRating,
      review: newReview,
      qualityRating: categoryRatings.qualityRating || null,
      valueRating: categoryRatings.valueRating || null,
      petSatisfactionRating: categoryRatings.petSatisfactionRating || null,
      ingredientsRating: categoryRatings.ingredientsRating || null,
    };

    let response;
    if (doesUserRatingExist.rating === 0) {
      response = await axiosInstance.post<RatingModel>(
        '/ratings/' + productId,
        payload,
        {
          responseType: 'json',
          useV2: false,
        }
      );
    } else {
      response = await axiosInstance.put<RatingModel>(
        '/ratings/' + productId,
        payload,
        {
          responseType: 'json',
          useV2: false,
        }
      );
    }

    return response.data;
  } catch (error) {
    console.error('Error updating user rating:', error);
    throw error;
  }
}
