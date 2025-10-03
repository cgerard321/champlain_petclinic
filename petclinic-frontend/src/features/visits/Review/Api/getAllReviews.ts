import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';

export const getAllReviews = async (): Promise<ReviewResponseDTO[]> => {
  const response = await axiosInstance.get<ReviewResponseDTO[]>(
    `/visits/reviews`,
    { useV2: false }
  );
  return response.data; // Return only the data
};
