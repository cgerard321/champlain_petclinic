import { Bill } from '@/features/bills/models/Bill.ts';
import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { BillRequestModel } from '@/features/bills/models/BillRequestModel.tsx';

export const updateBill = async (
  billId: string,
  bill: BillRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(`/bills/admin/${billId}`, bill);
};

export const getBill = async (billId: string): Promise<AxiosResponse<Bill>> => {
  return await axiosInstance.get<Bill>(
    `http://localhost:8080/api/v2/gateway/bills/admin/${billId}`
  );
};
