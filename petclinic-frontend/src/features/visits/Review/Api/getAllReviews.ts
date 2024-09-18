import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';

export const getAllReviews = async (): Promise<ReviewResponseDTO[]> => {
  const response = await axiosInstance.get<ReviewResponseDTO[]>(
    `http://localhost:8080/api/v2/gateway/visits/reviews`
  );
  return response.data; // Return only the data
};
