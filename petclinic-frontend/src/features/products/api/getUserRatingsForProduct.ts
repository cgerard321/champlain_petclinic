import { RatingModel } from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function getUserRatingsForProduct(
  productId: string
): Promise<RatingModel[]> {
  try {
    const res = await axiosInstance.get('/ratings/product/' + productId, {
      responseType: 'text',
      useV2: false,
    });

    return res.data
      .split('data:')
      .map((dataChunk: string) => {
        try {
          if (dataChunk == '') return null;
          return JSON.parse(dataChunk);
        } catch (err) {
          console.error('Could not parse JSON: ' + err);
        }
      })
      .filter((data?: JSON) => data !== null);
  } catch (error) {
    console.error('Error fetching user ratings for product:', error);
    throw error;
  }
}
