import axios, { AxiosResponse } from 'axios';
import { ReviewRequestDTO } from '../Model/ReviewRequestDTO';
import axiosInstance from '@/shared/api/axiosInstance';

export const addCustomerReview = async (
  ownerId: string,
  review: ReviewRequestDTO
): Promise<AxiosResponse<void>> => {
  try {
    const response = await axiosInstance.post<void>(
      `http://localhost:8080/api/v2/gateway/visits/owners/${ownerId}/reviews`,
      review,
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
    return response;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      console.error('Error response:', error.response);
    }
    throw error;
  }
};
