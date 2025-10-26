import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetRequestModel } from '@/features/customers/models/PetRequestModel.ts';

export const addPet = async (
  ownerId: string,
  pet: PetRequestModel
): Promise<AxiosResponse<PetRequestModel>> => {
  return axiosInstance.post(`owners/${ownerId}/pets`, pet, { useV2: false });
};
