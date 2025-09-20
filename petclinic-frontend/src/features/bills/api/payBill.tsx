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
      useV2: false,
    }
  );
  if (Array.isArray(response.data)) {
    response.data.filter((item: Bill) => item.billStatus === 'PAID');
  } else if (response.data && typeof response.data === 'object') {
    [response.data];
  } else {
    [];
  }
}
