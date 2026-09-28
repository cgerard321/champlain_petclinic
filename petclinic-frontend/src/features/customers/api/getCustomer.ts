import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerResponseModel } from '../models/CustomerResponseModel.ts';

export const getCustomer = async (
  customerId: string,
  includePhoto: boolean = false
): Promise<AxiosResponse<CustomerResponseModel>> => {
  return await axiosInstance.get<CustomerResponseModel>(
    `/customers/${customerId}`,
    {
      useV2: false,
      params: {
        _t: Date.now(),
        includePhoto,
      },
    }
  );
};
