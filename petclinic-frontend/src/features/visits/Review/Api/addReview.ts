import { AxiosResponse } from 'axios';
import { ReviewRequestDTO } from '../Model/ReviewRequestDTO';
import axiosInstance from '@/shared/api/axiosInstance';

export const addReview = async (
  review: ReviewRequestDTO
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>('/visits/reviews', review, {
    useV2: false,
  });
};
