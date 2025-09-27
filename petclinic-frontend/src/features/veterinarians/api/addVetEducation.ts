import axiosInstance from '@/shared/api/axiosInstance';
import { EducationRequestModel } from '@/features/veterinarians/models/EducationRequestModel';

export async function addVetEducation(
  education: EducationRequestModel
): Promise<EducationRequestModel> {
  const { vetId, ...educationData } = education;
  const response = await axiosInstance.post(
    `/vets/${vetId}/educations`,
    educationData,
    {
      useV2: false,
    }
  );
  return response.data;
}
