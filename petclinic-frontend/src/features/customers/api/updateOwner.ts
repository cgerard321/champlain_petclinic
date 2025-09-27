import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerRequestModel } from '../models/OwnerRequestModel';
import { OwnerResponseModel } from '../models/OwnerResponseModel';
import { OwnerUsernameRequestModel } from '../models/OwnerUsernameRequestModel';

export const updateOwner = async (
  userId: string,
  owner: OwnerRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(`/owners/${userId}`, owner);
};

export const getOwner = async (
  userId: string
): Promise<AxiosResponse<OwnerResponseModel>> => {
  return await axiosInstance.get<OwnerResponseModel>(`/owners/${userId}`);
};

export const updateOwnerUsername = async (
  userId: string,
  username: OwnerUsernameRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.patch<void>(`/users/${userId}/username`, username);
};
