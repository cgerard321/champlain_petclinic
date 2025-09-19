import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewRequestDTO } from '../Model/ReviewRequestDTO';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';

// Function to update a review
export const updateReview = async (
  reviewId: string,
  review: ReviewRequestDTO
): Promise<void> => {
  await axiosInstance.put<void>(`/visits/reviews/${reviewId}`, review);
};

// Function to get a review by ID
export const getReview = async (
  reviewId: string
): Promise<ReviewResponseDTO> => {
  const response = await axiosInstance.get<ReviewResponseDTO>(
    `/visits/reviews/${reviewId}`
  );
  return response.data; // Return only the data
};
