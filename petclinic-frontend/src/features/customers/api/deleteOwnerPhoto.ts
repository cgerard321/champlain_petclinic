import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerResponseModel } from '../models/OwnerResponseModel';

export const deleteOwnerPhoto = async (
  ownerId: string
): Promise<AxiosResponse<OwnerResponseModel>> => {
  return await axiosInstance.delete<OwnerResponseModel>(
    `/owners/${ownerId}/photo`,
    {
      useV2: false,
    }
  );
};
