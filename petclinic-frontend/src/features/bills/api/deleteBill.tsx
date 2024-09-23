import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function deleteBill(bill: Bill): Promise<any> {
    try {
        console.log(`Attempting to delete bill with ID: ${bill.billId}`);
        const response = await axiosInstance.delete(`/bills/${bill.billId}`);
        console.log(`Successfully deleted bill with ID: ${bill.billId}`);
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