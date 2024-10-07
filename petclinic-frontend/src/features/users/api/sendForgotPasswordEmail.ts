import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { UserRequestEmailModel } from '@/features/users/model/UserRequestEmailModel.ts';

export const sendForgotPasswordEmail = async (
  userRequestEmailModel: UserRequestEmailModel
): Promise<AxiosResponse<void>> => {
  const { email, url } = userRequestEmailModel;

  return await axiosInstance.post<void>(`/users/forgot_password`, {
    email,
    url,
  });
};
