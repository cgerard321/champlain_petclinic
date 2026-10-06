import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '@/features/bills/models/Bill.ts';

export async function getAllBillsByCustomerName(
    customerFirstName: string,
    customerLastName: string
): Promise<Bill[]> {
  const response = await axiosInstance.get(
    `/bills/customer/${customerFirstName}/${customerLastName}`,
    {
      responseType: 'stream',
      useV2: false,
    }
  );
  return response.data
    .split('data:')
    .map((payLoad: string) => {
      try {
        if (payLoad === '') return null;
        return JSON.parse(payLoad);
      } catch (err) {
        console.error("Can't parse JSON: " + err);
      }
    })
    .filter((data?: Bill) => data !== null);
}
