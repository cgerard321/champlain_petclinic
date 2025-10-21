import axiosInstance from '@/shared/api/axiosInstance';
import { BillRequestModel } from '../models/BillRequestModel';

export async function addBill(
  newBill: BillRequestModel,
  sendEmail: boolean
): Promise<void> {
  try {
    await axiosInstance.post(`/bills?sendEmail=${sendEmail}`, newBill, {
      headers: {
        'Content-Type': 'application/json',
      },
      useV2: false,
    });
  } catch (err) {
    console.error('Error creating bill:', err);
    throw err;
  }
}
