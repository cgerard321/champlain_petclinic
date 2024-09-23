import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function deleteBill(bill: Bill): Promise<any> {
  try {
    const response = await axiosInstance.delete(`/bills/${bill.billId}`);
    return response;
  } catch (error: any) {
    console.error('Error deleting bill:', error);
    if (error.response && error.response.data && error.response.data.message) {
      throw new Error(error.response.data.message);
    } else {
      throw new Error('Error deleting bill');
    }
  }
}
