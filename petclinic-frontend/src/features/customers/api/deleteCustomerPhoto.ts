import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerResponseModel } from '../models/CustomerResponseModel.ts';

export const deleteCustomerPhoto = async (
  customerId: string
): Promise<AxiosResponse<CustomerResponseModel>> => {
  return await axiosInstance.delete<CustomerResponseModel>(
    `/customers/${customerId}/photo`,
    {
      useV2: false,
    }
  );
};
