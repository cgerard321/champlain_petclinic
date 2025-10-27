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
    const response = await axiosInstance.get<string>(
      `/vets/${vetId}/ratings`,
      {
        responseType: 'text',
        useV2: true,
      } // Go through BFF v2
    );
    return response.data
      .split('data:')
      .map((payLoad: string) => {
        try {
          if (payLoad === '') return null;
          return JSON.parse(payLoad);
        } catch (err) {
          return null;
        }
      })
      .filter((data?: JSON) => data !== null);
  } catch (error) {
    return [];
  }
};
