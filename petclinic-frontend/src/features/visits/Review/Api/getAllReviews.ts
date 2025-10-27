import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';

export const getAllReviews = async (): Promise<ReviewResponseDTO[]> => {
  const response = await axiosInstance.get(
    `/visits/reviews`,
    {
      responseType: 'text',
      useV2: false
    }
  );

  const text = response.data;
  if (!text) return [];

  // Parse SSE format: extract data: lines
  const reviews: ReviewResponseDTO[] = [];
  const lines = text.split('\n');

  for (const line of lines) {
    if (line.startsWith('data:')) {
      try {
        const json = line.substring(5).trim();
        if (json) {
          reviews.push(JSON.parse(json));
        }
      } catch (e) {
        // skip invalid lines
      }
    }
  }

  return reviews;
};
