import axiosInstance from '@/shared/api/axiosInstance';

export async function payBill(
  customerId: string,
  billId: string,
  paymentDetails: { cardNumber: string; cvv: string; expirationDate: string }
): Promise<void> {
  // Basic validation
  if (
    !customerId ||
    !billId ||
    !paymentDetails.cardNumber ||
    !paymentDetails.cvv ||
    !paymentDetails.expirationDate
  ) {
    throw new Error('Invalid payment details');
  }

  try {
    // Make API call to update bill status to PAID
    const response = await axiosInstance.post(
      `/bills/customer/${customerId}/bills/${billId}/pay`,
      {
        cardNumber: paymentDetails.cardNumber,
        cvv: paymentDetails.cvv,
        expirationDate: paymentDetails.expirationDate,
      },
      {
        headers: { 'Content-Type': 'application/json' },
        useV2: false,
      }
    );

    if (!response || response.status !== 200) {
      throw new Error(
        `Payment failed: ${response.status} ${response.statusText}`
      );
    }

    return Promise.resolve();
  } catch (error) {
    console.error('Payment processing error:', error);
    throw new Error('Payment processing failed. Please try again.');
  }
}
