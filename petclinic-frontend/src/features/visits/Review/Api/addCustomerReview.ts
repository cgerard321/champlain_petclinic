import { AxiosResponse } from 'axios';
import { ReviewRequestDTO } from '../Model/ReviewRequestDTO';
import axiosInstance from '@/shared/api/axiosInstance';

export const addCustomerReview = async (
  ownerId: string,
  review: ReviewRequestDTO
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post(`/visits/owners/${ownerId}/reviews`, review, {
    useV2: true,
  });
};
