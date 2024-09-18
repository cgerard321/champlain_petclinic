import { RatingModel } from '@/features/inventories/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { getUserRating } from './getUserRating';

export async function updateUserRating(
  productId: string,
  newRating: number
): Promise<number> {
  const doesExist = await getUserRating(productId);
  if (doesExist == 0) {
    const res = await axiosInstance.post<RatingModel>(
      '/ratings/' + productId,
      { rating: newRating },
      {
        responseType: 'json',
      }
    );
    switch (res.status) {
      case 401:
        console.error('Could not get token, unauthorized..');
        return 0;
      case 404:
        return 0;
      case 422:
        console.error('IDs are invalid');
        return 0;
      case 201:
        return res.data.rating;
      default:
        return 0;
    }
  } else {
    const res = await axiosInstance.put<RatingModel>(
      '/ratings/' + productId,
      { rating: newRating },
      {
        responseType: 'json',
      }
    );
    switch (res.status) {
      case 401:
        console.error('Could not get token, unauthorized..');
        return 0;
      case 404:
        return 0;
      case 422:
        console.error('IDs are invalid');
        return 0;
      case 200:
        return res.data.rating;
      default:
        return 0;
    }
  }
}
