import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Bill } from '@/features/bills/models/Bill.ts';

export async function getAllBillsByCustomer(customerId: string): Promise<Bill[]> {
    const response = await axiosInstance.get<Bill[]>(
        `bills/customer/${customerId}`
    );
    return response.data;
}