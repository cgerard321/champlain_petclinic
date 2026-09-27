import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerModel } from '@/features/customers/models/CustomerModel.ts';

export const addCustomer = async (
  customer: CustomerModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>('/customers', customer, {
    useV2: false,
  });
};
