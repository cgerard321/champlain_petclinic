import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const updateUsername = async (
  userId: string,
  username: string
): Promise<AxiosResponse<string>> => {
  return await axiosInstance.patch<string>(
    `/users/${userId}/username`,
    username,
    {
      useV2: false,
      headers: {
        'Content-Type': 'text/plain',
      },
    }
  );
};
