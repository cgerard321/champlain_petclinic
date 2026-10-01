import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '@/features/bills/models/Bill.ts';

export async function getAllBillsStream(
  billId?: string,
  customerId?: string,
  ownerFirstName?: string,
  ownerLastName?: string,
  visitType?: string,
  vetId?: string,
  vetFirstName?: string,
  vetLastName?: string
): Promise<Bill[]> {
  const response = await axiosInstance.get('/bills/stream', {
    params: {
      billId,
      customerId,
      ownerFirstName,
      ownerLastName,
      visitType,
      vetId,
      vetFirstName,
      vetLastName,
    },
    responseType: 'text',
    useV2: false,
  });

  return response.data
    .split('data:')
    .map((payload: string) => {
      try {
        const trimmedPayload = payload.trim();

        if (!trimmedPayload) {
          return null;
        }

        return JSON.parse(trimmedPayload);
      } catch (err) {
        console.error("Can't parse bill stream payload:", err);
        return null;
      }
    })
    .filter((data: Bill | null): data is Bill => data !== null);
}
