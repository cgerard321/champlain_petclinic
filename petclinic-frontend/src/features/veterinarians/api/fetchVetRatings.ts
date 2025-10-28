import axiosInstance from '@/shared/api/axiosInstance';

export interface RatingResponseType {
  ratingId: string;
  rateScore: number;
  rateDescription: string;
  predefinedDescription: string;
  rateDate: string;
  customerName: string;
}

/**
 * Fetch ratings for a specific veterinarian by ID.
 */
export const fetchVetRatings = async (
  vetId: string
): Promise<RatingResponseType[]> => {
  try {
    const response = await axiosInstance.get<RatingResponseType[]>(
      `/vets/${vetId}/ratings`,
      { useV2: true } // Go through BFF v2
    );
    return response.data ?? [];
  } catch (error) {
    console.error('Failed to fetch vet ratings:', error);
    throw new Error('Failed to fetch vet ratings');
  }
};
