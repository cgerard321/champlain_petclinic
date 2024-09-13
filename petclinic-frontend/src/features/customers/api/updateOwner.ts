import axios, { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerRequestModel } from '../models/OwnerRequestModel';
import { OwnerResponseModel } from '../models/OwnerResponseModel';

export const updateOwner = async (
  userId: string,
  owner: OwnerRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(`/owners/${userId}`, owner);
};

export const getOwner = async (
  userId: string
): Promise<AxiosResponse<OwnerResponseModel>> => {
  return await axios.get<OwnerResponseModel>(
    `http://localhost:8080/api/gateway/owners/${userId}`
  );
};
