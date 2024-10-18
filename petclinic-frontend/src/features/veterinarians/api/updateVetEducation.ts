import axios, { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { EducationRequestModel } from '../models/EducationRequestModel';

export const updateVetEducation = async (
  vetId: string,
  educationId: string,
  education: EducationRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(
    `/vets/${vetId}/educations/${educationId}`,
    education
  );
};

export const getEducation = async (
  vetId: string
): Promise<AxiosResponse<EducationRequestModel>> => {
  return await axios.get<EducationRequestModel>(
    `http://localhost:8080/api/gateway/vet/${vetId}/education`
  );
};
