import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { UserDetailsModel } from '../models/UserDetailsModel';

export const getUserDetails = async (
  userId: string
): Promise<AxiosResponse<UserDetailsModel>> => {
  return await axiosInstance.get<UserDetailsModel>(`/users/${userId}`, {
    useV2: false,
  });
};
