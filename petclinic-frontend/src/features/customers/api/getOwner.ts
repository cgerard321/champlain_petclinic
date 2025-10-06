import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerResponseModel } from '../models/OwnerResponseModel';

export const getOwner = async (
  ownerId: string
): Promise<AxiosResponse<OwnerResponseModel>> => {
  return await axiosInstance.get<OwnerResponseModel>(`/owners/${ownerId}`, {
    useV2: false,
    // Add cache-busting parameter to ensure fresh data
    params: {
      _t: Date.now(),
    },
  });
};
