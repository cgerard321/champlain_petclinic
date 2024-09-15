import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

import { OwnerResponseModel } from '../models/OwnerResponseModel';

export const getAllOwners = async (): Promise<
  AxiosResponse<OwnerResponseModel[]>
> => {
  return await axiosInstance.get<OwnerResponseModel[]>(
    `http://localhost:8080/api/gateway/owners`
  );
};
