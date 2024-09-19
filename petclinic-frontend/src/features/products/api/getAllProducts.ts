import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getAllProducts(
  minPrice?: number,
  maxPrice?: number
): Promise<ProductModel[]> {
  const params: any = {};
  if (minPrice !== undefined) params.minPrice = minPrice;
  if (maxPrice !== undefined) params.maxPrice = maxPrice;

  const res = await axiosInstance.get('/products', {
    responseType: 'stream',
    params,
  });

  const dataChunks = res.data.split('\n\n');
  const products: ProductModel[] = [];

  // Iterate over each chunk of data
  for (const chunk of dataChunks) {
    if (chunk.trim() === '') continue;
    const dataLine = chunk
      .trim()
      .split('\n')
      .find(line => line.startsWith('data:'));
    if (dataLine) {
      const jsonString = dataLine.replace('data:', '').trim();
      try {
        const product = JSON.parse(jsonString);
        products.push(product);
      } catch (err) {
        console.error('Could not parse JSON:', err);
      }
    }
  }

  return products;
}
