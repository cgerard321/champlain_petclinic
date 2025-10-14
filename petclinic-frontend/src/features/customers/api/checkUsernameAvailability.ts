import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const checkUsernameAvailability = async (
  username: string
): Promise<AxiosResponse<boolean>> => {
  return await axiosInstance.get<boolean>(
    `/users/username/check?username=${encodeURIComponent(username)}`,
    {
      useV2: false,
    }
  );
};
