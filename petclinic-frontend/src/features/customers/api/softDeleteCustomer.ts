import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const softDeleteCustomer = async (
  customerId: string
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.delete<void>(`/customers/${customerId}/soft`, {
    useV2: true,
  });
};
