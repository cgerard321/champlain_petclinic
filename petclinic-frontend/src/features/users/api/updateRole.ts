import { AxiosResponse } from 'axios';
import { RoleResponseModel } from '@/features/users/model/RoleResponseModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const updateRole = async (
  id: number,
  name: string
): Promise<AxiosResponse<RoleResponseModel>> => {
  return await axiosInstance.patch<RoleResponseModel>(`/roles/${id}`, {
    name: name,
  });
};
