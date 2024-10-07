import { RatingModel } from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function getUserRating(productId: string): Promise<RatingModel> {
  const res = await axiosInstance.get<RatingModel>('/ratings/' + productId, {
    responseType: 'json',
  });
  const emptyResponse = { rating: 0, review: '' };
  switch (res.status) {
    case 200:
      return res.data.rating == null ? emptyResponse : res.data;
    case 422:
      console.error('IDs are invalid');
      return emptyResponse;
    case 401:
      console.error('Could not get token, unauthorized..');
      return emptyResponse;
    default:
      return emptyResponse;
  }
}
