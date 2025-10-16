import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';

export async function getAllProducts(
  minPrice?: number,
  maxPrice?: number,
  minRating?: number,
  maxRating?: number,
  //sort?: string,
  deliveryType?: string,
  productType?: string
): Promise<ProductModel[]> {
  // Build query parameters based on provided filters from within the function
  const params: Record<string, unknown> = {};
  if (minPrice !== undefined && minPrice !== null) params.minPrice = minPrice;
  if (maxPrice !== undefined && maxPrice !== null) params.maxPrice = maxPrice;
  if (minRating !== undefined && minRating !== null)
    params.minRating = minRating;
  if (maxRating !== undefined && maxRating !== null)
    params.maxRating = maxRating;
  // if (sort) params.sort = sort;
  if (deliveryType && deliveryType !== 'default')
    params.deliveryType = deliveryType;
  if (productType && productType !== 'default')
    params.productType = productType;

  try {
    const response = await axiosInstance.get('/products', {
      params,
      useV2: false,
    });

    return response.data
      .split('data:')
      .map((dataChunk: string) => {
        try {
          if (dataChunk == '') return null;
          return JSON.parse(dataChunk);
        } catch (error) {
          console.error('Could not parse JSON: ' + error);
        }
      })
      .filter((data?: JSON) => data !== null);
  } catch (error) {
    console.error('Error fetching products:', error);
    throw error;
  }
}
