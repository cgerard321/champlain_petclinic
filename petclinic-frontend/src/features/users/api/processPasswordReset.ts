import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { UserPasswordAndTokenRequestModel } from '@/features/users/model/UserPasswordAndTokenRequestModel.ts';

export const processPasswordReset = async (
  userPasswordAndTokenRequestModel: UserPasswordAndTokenRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>(
    `/users/reset_password`,
    userPasswordAndTokenRequestModel
  );
};
