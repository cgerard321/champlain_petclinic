import { RoleResponseModel } from '@/features/users/model/RoleResponseModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { AxiosResponse } from 'axios';

export const getRoleById = async (
  id: number
): Promise<AxiosResponse<RoleResponseModel>> => {
  return await axiosInstance.get<RoleResponseModel>(`/roles/${id}`);
};
