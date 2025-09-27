import axiosInstance from '@/shared/api/axiosInstance';
import { EducationRequestModel } from '../models/EducationRequestModel';
import { EducationResponseModel } from '../models/EducationResponseModel';

export const updateVetEducation = async (
  vetId: string,
  educationId: string,
  education: EducationRequestModel
): Promise<EducationRequestModel> => {
  return await axiosInstance.put(
    `/vets/${vetId}/educations/${educationId}`,
    education,
    {
      useV2: false,
    }
  );
};

export const getEducation = async (
  vetId: string
): Promise<EducationResponseModel> => {
  const response = await axiosInstance.get<EducationResponseModel>(
    `/vets/${vetId}/education`
  );
  return response.data;
};
