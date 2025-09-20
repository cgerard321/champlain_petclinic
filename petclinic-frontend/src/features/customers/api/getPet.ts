import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel.ts';

export const getPet = async (
  petId: string
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.get<PetResponseModel>(`/pets/${petId}`, {
    useV2: true,
  });
};
