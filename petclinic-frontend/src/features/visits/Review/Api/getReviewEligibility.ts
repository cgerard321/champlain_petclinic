import axiosInstance from '@/shared/api/axiosInstance';

export interface ReviewEligibilityResponse {
  eligible: boolean;
}

export const getReviewEligibility =
  async (): Promise<ReviewEligibilityResponse> => {
    const response = await axiosInstance.get<ReviewEligibilityResponse>(
      '/visits/reviews/eligibility',
      {
        useV2: false,
        handleLocally: true,
      }
    );

    return response.data;
  };
