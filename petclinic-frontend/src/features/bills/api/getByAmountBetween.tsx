import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance';

export default async function getBillsByAmountRange(
  customerId: string,
  minAmount: number,
  maxAmount: number
): Promise<Bill[]> {
  try {
    const response = await axiosInstance.get(
      `/customers/${customerId}/bills/filter-by-amount`,
      {
        params: { minAmount, maxAmount },
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
    console.error('getBillsByAmountRange error', error);
    return [];
  }
}
