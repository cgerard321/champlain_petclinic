import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerResponseModel } from '../models/CustomerResponseModel.ts';

export const deleteCustomer = async (
  customerId: string
): Promise<AxiosResponse<CustomerResponseModel>> => {
  return await axiosInstance.delete<CustomerResponseModel>(
    `/customers/${customerId}`,
    {
      useV2: true,
    }
  );
};
