import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '@/features/bills/models/Bill.ts';

export async function getBillsByMonth(
  year: number,
  month: number
): Promise<Bill[]> {
  const response = await axiosInstance.get(`/bills/admin/month`, {
    params: { year, month },
    responseType: 'text',
    useV2: true,
  });

  return response.data
    .split('data:')
    .map((payLoad: string) => {
      try {
        if (payLoad.trim() === '') return null;
        return JSON.parse(payLoad);
      } catch (err) {
        console.error("Can't parse JSON: " + err);
      }
    })
    .filter((data?: Bill) => data !== null);
}
