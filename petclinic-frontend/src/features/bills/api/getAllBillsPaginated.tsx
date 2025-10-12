import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '../models/Bill';

export async function getAllBillsPaginated(
  currentPage: number,
  listSize: number
): Promise<Bill[]> {
  const response = await axiosInstance.get<Bill[]>(
    `/bills?page=${currentPage}&size=${listSize}`,
    { useV2: true }
  );
  return response.data;
}
