import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance';

export default async function getBillsByDueDateRange(
  customerId: string,
  startDate: string,
  endDate: string
): Promise<Bill[]> {
  try {
    const response = await axiosInstance.get(
      `/customers/${customerId}/bills/filter-by-due-date`,
      {
        params: { startDate, endDate },
        headers: { Accept: 'application/json' },
        useV2: true,
      }
    );

    if (!response || !response.data) return [];

    if (Array.isArray(response.data)) {
      return response.data as Bill[];
    }

    return [response.data as Bill];
  } catch (error) {
    console.error('getBillsByDueDateRange error', error);
    return [];
  }
}
