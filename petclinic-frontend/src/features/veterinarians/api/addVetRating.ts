import axiosInstance from '@/shared/api/axiosInstance';
import { VetRatingRequestModel } from '@/features/veterinarians/models/VetRatingRequestModel';

export interface VetRatingResponseModel {
  ratingId: string;
  rateScore: number;
  rateDescription: string;
  predefinedDescription?: string;
  rateDate?: string;
  customerName?: string;
}

export const addVetRating = async (
  rating: VetRatingRequestModel
): Promise<VetRatingResponseModel> => {
  const { vetId, ...ratingFields } = rating;
  const payload = {
    ...ratingFields,
    vetId,
  };

  const { data } = await axiosInstance.post<VetRatingResponseModel>(
    `/vets/${vetId}/ratings`,
    payload,
    { useV2: false }
  );

  return data;
};
