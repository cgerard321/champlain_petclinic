import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerResponseModel } from '../models/OwnerResponseModel';

export const getOwner = async (
  ownerId: string,
  includePhoto: boolean = false
): Promise<AxiosResponse<OwnerResponseModel>> => {
  return await axiosInstance.get<OwnerResponseModel>(`/owners/${ownerId}`, {
    useV2: false,
    params: {
      _t: Date.now(),
      includePhoto,
    },
  });
};
