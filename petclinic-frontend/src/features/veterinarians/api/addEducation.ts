import axiosInstance from '@/shared/api/axiosInstance';
import { EducationRequestModel } from '@/features/veterinarians/models/EducationRequestModel';

export async function addVetEducation(
  vetId: string,
  education: EducationRequestModel
): Promise<EducationRequestModel> {
  const response = await axiosInstance.post(
    `/vets/${vetId}/educations`,
    education
  );
  return response.data;
}
