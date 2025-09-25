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
      //body: JSON.stringify(paymentDetails),
    }
  );
  return response.data.filter((item: Bill) => item.billStatus === 'PAID');

  /*const response = await fetch(
    `http://localhost:8080/api/v2/gateway/bills/customer/${customerId}/bills/${billId}/pay`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(paymentDetails),
    }
  );

  if (!response.ok) {
    throw new Error('Payment failed');
  }
    */
}
