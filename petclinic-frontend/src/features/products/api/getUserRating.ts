import { RatingModel } from '@/features/inventories/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function getUserRating(productId: string): Promise<number> {
  const res = await axiosInstance.get<RatingModel>('/ratings/' + productId, {
    responseType: 'json',
  });
  switch (res.status) {
    case 200:
      return res.data.rating == null ? 0 : res.data.rating;
    case 422:
      console.error('IDs are invalid');
    case 401:
      console.error('Could not get token, unauthorized..');
    default:
      return 0;
  }
}
