import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerRequestModel } from '../models/CustomerRequestModel.ts';

export const updateCustomer = async (
  customerId: string,
  customer: CustomerRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(`/customers/${customerId}`, customer, {
    useV2: false,
  });
};
