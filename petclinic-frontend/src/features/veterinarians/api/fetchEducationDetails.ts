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
    const response = await axiosInstance.get<string>(
      `/vets/${vetId}/educations`,
      {
        responseType: 'text',
        useV2: false,
      }
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
