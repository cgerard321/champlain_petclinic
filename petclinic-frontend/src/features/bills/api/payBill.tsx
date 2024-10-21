export async function payBill(
  customerId: string,
  billId: string,
  paymentDetails: { cardNumber: string; cvv: string; expirationDate: string }
): Promise<void> {
  const response = await fetch(
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
}
