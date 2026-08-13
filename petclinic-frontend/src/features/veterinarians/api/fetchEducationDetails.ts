import axiosInstance from '@/shared/api/axiosInstance';

export interface EducationResponseType {
  educationId: string;
  vetId: string;
  schoolName: string;
  degree: string;
  fieldOfStudy: string;
  startDate: string;
  endDate: string;
}

export const fetchEducationDetails = async (
  vetId: string
): Promise<EducationResponseType[]> => {
  try {
    const response = await axiosInstance.get<EducationResponseType[]>(
      `/vets/${vetId}/educations`,
      { useV2: false }
    );
    return response.data ?? [];
  } catch (error) {
    console.error('Failed to fetch education details:', error);
    throw new Error('Failed to fetch education details');
  }
};
