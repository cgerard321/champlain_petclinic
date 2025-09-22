import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function getBillByBillId(billId: string): Promise<Bill | null> {
  const response = await axiosInstance.get(`/bills/admin/${billId}`, {
    responseType: 'stream',
    useV2: false,
  });

  try {
    const billData = response.data;
    return JSON.parse(billData);
  } catch (err) {
    console.error("Can't parse JSON: " + err);
    return null;
  }
}
