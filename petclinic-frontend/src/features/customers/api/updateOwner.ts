import axios from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerRequestModel } from '../models/OwnerRequestModel.ts';
import { OwnerResponseModel } from '../models/OwnerResponseModel.ts';

export const updateOwner = async (userId: string, owner: OwnerRequestModel) => {
  return await axiosInstance.put(`/owners/${userId}`, owner);
};

export const getOwner = async (userId: string) => {
  return await axios.get<OwnerResponseModel>(`http://localhost:8080/api/gateway/owners/${userId}`);
};
