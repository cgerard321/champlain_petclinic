import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerRequestModel } from '../models/OwnerRequestModel';

export const updateOwner = async (
  ownerId: string,
  owner: OwnerRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(`/owners/${ownerId}`, owner, {
    useV2: false,
  });
};
