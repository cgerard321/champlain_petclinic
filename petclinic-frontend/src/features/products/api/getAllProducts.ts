import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getAllProducts(
  minPrice?: number,
  maxPrice?: number
): Promise<ProductModel[]> {
  const params: Record<string, number> = {};
  if (minPrice !== undefined) params.minPrice = minPrice;
  if (maxPrice !== undefined) params.maxPrice = maxPrice;

  const res = await axiosInstance.get('/products', {
    responseType: 'stream',
    params,
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
}
