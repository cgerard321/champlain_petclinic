import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Bill } from '../models/Bill';

export async function getAllBillsPaginated(
  currentPage: number,
  listSize: number
): Promise<Bill[]> {
  const url = `bills?page=${currentPage}&size=${listSize}`;

  const response = await axiosInstance.get<Bill[]>(
    axiosInstance.defaults.baseURL + url
  );
  return response.data;
}
