import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getAllProducts(
  minPrice?: number,
  maxPrice?: number,
  sort?: string
): Promise<ProductModel[]> {
  const params: Record<string, any> = {};
  if (minPrice !== undefined && minPrice !== null) params.minPrice = minPrice;
  if (maxPrice !== undefined && maxPrice !== null) params.maxPrice = maxPrice;
  if (sort) params.sort = sort;

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
