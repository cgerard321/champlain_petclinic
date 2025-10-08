import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '@/features/bills/models/Bill.ts';

export const getAllBillsByVetId = async (vetId: string): Promise<Bill[]> => {
  try {
    const response = await axiosInstance.get(`/bills/vets/${vetId}`, {
      responseType: 'stream',
      useV2: false,
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching bills by vet ID:', error);
    throw error;
  }
};
