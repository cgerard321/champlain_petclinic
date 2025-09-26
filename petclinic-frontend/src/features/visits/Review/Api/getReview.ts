import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';

// Function to get a review by ID
export const getReview = async (
  reviewId: string
): Promise<ReviewResponseDTO> => {
  const response = await axiosInstance.get<ReviewResponseDTO>(
    `/reviews/${reviewId}`,
    { useV2: true }
  );
  return response.data; // Return only the data
};
