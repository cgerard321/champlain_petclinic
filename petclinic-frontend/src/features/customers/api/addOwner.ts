import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerModel } from '@/features/customers/models/OwnerModel.ts';

export const addOwner = async (
  owner: OwnerModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>('/owners', owner, { useV2: false });
};
