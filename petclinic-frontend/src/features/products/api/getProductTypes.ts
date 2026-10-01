import axiosInstance from '@/shared/api/axiosInstance';
import { ProductTypeModel } from '@/features/products/models/ProductModels/ProductTypeModel';

export async function getProductTypes(): Promise<ProductTypeModel[]> {
  try {
    const response = await axiosInstance.get<string>('/products/types', {
      useV2: false,
      responseType: 'text',
    });

    return response.data
      .split('data:')
      .map(chunk => chunk.trim())
      .filter(chunk => chunk !== '')
      .map(chunk => JSON.parse(chunk) as ProductTypeModel);
  } catch (error) {
    console.error('Could not fetch product types', error);
    throw error;
  }
}
