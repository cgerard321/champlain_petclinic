import axiosInstance from '@/shared/api/axiosInstance';
import { EducationResponseModel } from '@/features/veterinarians/models/EducationResponseModel';
export const deleteVetEducation = async (
  vetId: string,
  educationId: string
): Promise<EducationResponseModel> => {
  const response = await axiosInstance.delete(
    `/vets/${vetId}/educations/${educationId}`,
    {
      useV2: false,
    }
  );
  return response.data;
};
