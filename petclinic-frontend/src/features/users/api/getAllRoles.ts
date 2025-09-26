import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { RoleResponseModel } from '@/features/users/model/RoleResponseModel.ts';

export const getAllRoles = async (): Promise<
  AxiosResponse<RoleResponseModel[]>
> => {
  return await axiosInstance.get<RoleResponseModel[]>('/roles', {
    useV2: true,
  });
};
