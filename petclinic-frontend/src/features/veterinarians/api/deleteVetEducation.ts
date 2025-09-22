import axiosInstance from '@/shared/api/axiosInstance';
import { EducationResponseModel } from '@/features/veterinarians/models/EducationResponseModel';
export const deleteVetEducation = async (
  vetId: string,
  educationId: string
): Promise<EducationResponseModel> => {
  return await axiosInstance.delete(`/vets/${vetId}/educations/${educationId}`);
};
