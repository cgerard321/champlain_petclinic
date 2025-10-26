import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '@/features/bills/models/Bill.ts';

export async function getAllBillsByVisitType(
  visitType: string
): Promise<Bill[]> {
  const response = await axiosInstance.get(`/bills/${visitType}/visitType`, {
    responseType: 'stream',
    useV2: false,
  });
  return response.data
    .split('data:')
    .map((payload: string) => {
      try {
        if (payload === '') return null;
        return JSON.parse(payload);
      } catch (err) {
        console.error("Can't parse JSON: " + err);
      }
    })
    .filter((data?: Bill) => data !== null);
}
