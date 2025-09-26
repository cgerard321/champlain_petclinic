import { AxiosResponse } from 'axios';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';
import axiosInstance from '@/shared/api/axiosInstance';

export const deleteReview = async (
  reviewId: string
): Promise<AxiosResponse<ReviewResponseDTO>> => {
  return await axiosInstance.delete<ReviewResponseDTO>(
    `/visits/reviews/${reviewId}`,
    {
      useV2: false,
    }
  );
};
