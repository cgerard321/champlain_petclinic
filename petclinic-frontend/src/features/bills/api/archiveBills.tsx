import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '../models/Bill';

export async function archiveBills(): Promise<Bill[]> {
  try {
    const response = await axiosInstance.patch<Bill[]>(
      '/bills/archive',
      {},
      {
        useV2: false, // Use v1 API explicitly (preferred for backwards compatibility)
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error archiving bills:', error);
    throw error;
  }
}
