import axiosInstance from '@/shared/api/axiosInstance';
import { ProductEnumsModel } from '@/features/products/models/ProductModels/ProductEnumsModel';

export async function getProductEnums(): Promise<ProductEnumsModel> {
  try{
      const response = await axiosInstance.get<ProductEnumsModel>(
          '/products/enums',
          { useV2: false }
      );
  return response.data;
}catch(error) {
      console.error('Failed to fetch product enums:', error);
      throw error;
  }
}
