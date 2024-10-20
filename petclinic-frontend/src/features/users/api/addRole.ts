import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { RoleRequestModel } from '@/features/users/model/RoleRequestModel';
import { RoleResponseModel } from '@/features/users/model/RoleResponseModel';

export const addRole = async (
  role: RoleRequestModel
): Promise<AxiosResponse<RoleResponseModel>> => {
  return await axiosInstance.post<RoleResponseModel>('/roles', role);
};
