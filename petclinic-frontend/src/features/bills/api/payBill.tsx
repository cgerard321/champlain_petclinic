import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '../models/Bill';

export async function payBill(
  customerId: string,
  billId: string,
  paymentDetails: { cardNumber: string; cvv: string; expirationDate: string }
): Promise<void> {
  const response = await axiosInstance.post(
    `/bills/customer/${customerId}/bills/${billId}/pay`,
    paymentDetails,
    {
      headers: { 'Content-Type': 'application/json' },
      withCredentials: true,
      useV2: true,
    }
  );
  return response.data.filter((item: Bill) => item.billStatus === 'PAID');
}
