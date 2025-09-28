import axiosInstance from '@/shared/api/axiosInstance';
import { ReviewResponseDTO } from '../Model/ReviewResponseDTO';
import { useUser } from '@/context/UserContext';

export const getAllCustomerReviews = async (
  userId: string
): Promise<ReviewResponseDTO[]> => {
  const response = await axiosInstance.get<ReviewResponseDTO[]>(
    `/visits/owners/${userId}/reviews`,
    { useV2: false }
  );
  return response.data; // Return only the data
};

export const useGetAllCustomerReviews = (): (() => Promise<
  ReviewResponseDTO[]
>) => {
  const { user } = useUser();
  return () => getAllCustomerReviews(user.userId);
};
