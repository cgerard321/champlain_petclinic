import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetRequestModel } from '@/features/customers/models/PetRequestModel.ts';

export const addPet = async (
  pet: PetRequestModel
): Promise<AxiosResponse<PetRequestModel>> => {
  return axiosInstance.post(`/pets`, pet, { useV2: false });
};
