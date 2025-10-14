import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewRequestDTO } from '../Model/ReviewRequestDTO';

// Function to update a review
export const updateReview = async (
  reviewId: string,
  review: ReviewRequestDTO
): Promise<void> => {
  await axiosInstance.put<void>(`/visits/reviews/${reviewId}`, review, {
    useV2: false,
  });
};
