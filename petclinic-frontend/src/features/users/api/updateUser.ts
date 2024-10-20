import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { UserPasswordLessDTO } from '@/features/users/model/UserPasswordLessDTO';

export const updateUser = async (
  userId: string,
  user: UserPasswordLessDTO
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(`/users/${userId}`, user);
};

export const getUser = async (
  userId: string
): Promise<AxiosResponse<UserPasswordLessDTO>> => {
  return await axiosInstance.get<UserPasswordLessDTO>(`/users/${userId}`);
};
