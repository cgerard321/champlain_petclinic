import axiosInstance from '@/shared/api/axiosInstance';
import { BillRequestModel } from '../models/BillRequestModel';

export async function addBill(newBill: BillRequestModel): Promise<void> {
  try {
    await axiosInstance.post('/bills', newBill, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
  } catch (err) {
    console.error('Error creating bill:', err);
    throw err;
  }
}
