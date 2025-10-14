import axios from 'axios';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v2/gateway',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function patchGetProduct(
  productId: string
): Promise<ProductModel> {
  try {
    const response = await axiosInstance.patch<ProductModel>(
      `/products/${productId}/decrease`,
      {},
      {
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'GET,PUT,POST,DELETE,PATCH,OPTIONS',
          useV2: false,
        },
      }
    );

    return response.data;
  } catch (error) {
    console.error('Error patching product:', error);
    throw error;
  }
}

// This page needs to be deleted since react is only for customers and not employees.
