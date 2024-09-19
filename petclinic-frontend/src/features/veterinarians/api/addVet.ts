import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel.ts';

export const addVet = async (
  vet: VetResponseModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>('/vets', vet);
};
