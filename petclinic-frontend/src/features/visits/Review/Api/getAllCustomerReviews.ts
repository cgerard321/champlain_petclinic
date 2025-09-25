import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';
import { useUser } from '@/context/UserContext';

export const getAllReviews = async (
  userId: string
): Promise<ReviewResponseDTO[]> => {
  const response = await axiosInstance.get<ReviewResponseDTO[]>(
    `/visits/owners/${userId}/reviews`,
    { useV2: false }
  );
  return response.data; // Return only the data
};

export const useGetAllReviews = (): (() => Promise<ReviewResponseDTO[]>) => {
  const { user } = useUser();
  return () => getAllReviews(user.userId);
};
