import axios, {AxiosResponse} from 'axios';
import {ReviewRequestDTO} from '../Model/ReviewRequestDTO';
import axiosInstance from '@/shared/api/axiosInstance';

export const addCustomerReview = async (
  ownerId: string,
  review: ReviewRequestDTO
): Promise<AxiosResponse<void>> => {
  try {
    return await axiosInstance.post<void>(
        `/visits/owners/${ownerId}/reviews`,
        review,
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
    );
  } catch (error) {
    if (axios.isAxiosError(error)) {
      console.error('Error response:', error.response);
    }
    throw error;
  }
};
