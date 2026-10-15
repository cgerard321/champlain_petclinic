import { RatingModel } from '@/features/products/models/ProductModels/RatingModel';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function getUserRatingsForProduct(
  productId: string,
  personalized = false
): Promise<RatingModel[]> {
  try {
    const suffix = personalized ? '/personalized' : '';
    const res = await axiosInstance.get(
      '/ratings/product/' + productId + suffix,
      {
        responseType: 'text',
        useV2: false,
        // Let callers fall back to the public list if the session is stale
        handleLocally: personalized,
      }
    );

    return res.data
      .split(/^data:\s?/m)
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
