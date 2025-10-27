import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '../models/Bill';

export async function getAllBillsPaginated(
  currentPage: number,
  listSize: number,
  billId?: string,
  customerId?: string,
  ownerFirstName?: string,
  ownerLastName?: string,
  visitType?: string,
  vetId?: string,
  vetFirstName?: string,
  vetLastName?: string
): Promise<Bill[]> {
  const params: Record<string, string | number> = {
    page: currentPage,
    size: listSize,
  };

  if (billId) params.billId = billId;
  if (customerId) params.customerId = customerId;
  if (ownerFirstName) params.ownerFirstName = ownerFirstName;
  if (ownerLastName) params.ownerLastName = ownerLastName;
  if (visitType) params.visitType = visitType;
  if (vetId) params.vetId = vetId;
  if (vetFirstName) params.vetFirstName = vetFirstName;
  if (vetLastName) params.vetLastName = vetLastName;

  const response = await axiosInstance.get<Bill[]>('/bills', {
    params,
    useV2: true,
  });

  return response.data;
}
