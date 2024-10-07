import { RatingModel } from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { getUserRating } from './getUserRating';

export async function updateUserRating(
  productId: string,
  newRating: number,
  newReview: string | null
): Promise<RatingModel> {
  const doesExist = await getUserRating(productId);
  const emptyResponse = { rating: 0, review: '' };
  if (doesExist.rating == 0) {
    const res = await axiosInstance.post<RatingModel>(
      '/ratings/' + productId,
      { rating: newRating, review: newReview },
      {
        responseType: 'json',
      }
    );
    switch (res.status) {
      case 401:
        console.error('Could not get token, unauthorized..');
        return emptyResponse;
      case 404:
        return emptyResponse;
      case 422:
        console.error('IDs are invalid');
        return emptyResponse;
      case 201:
        return res.data;
      default:
        return emptyResponse;
    }
  } else {
    const res = await axiosInstance.put<RatingModel>(
      '/ratings/' + productId,
      { rating: newRating, review: newReview },
      {
        responseType: 'json',
      }
    );
    switch (res.status) {
      case 401:
        console.error('Could not get token, unauthorized..');
        return emptyResponse;
      case 404:
        return emptyResponse;
      case 422:
        console.error('IDs are invalid');
        return emptyResponse;
      case 200:
        return res.data;
      default:
        return emptyResponse;
    }
  }
}
