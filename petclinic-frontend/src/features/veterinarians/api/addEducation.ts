import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { EducationRequestModel } from '../models/EducationRequestModel';

export const addVetEducation = async (
  vetId: string,
  education: EducationRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>(`/vets/${vetId}/educations`, education);
};
