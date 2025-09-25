import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetRequestModel } from '@/features/customers/models/PetRequestModel';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';

export const addPetForOwner = async (
  ownerId: string,
  pet: PetRequestModel
): Promise<AxiosResponse<PetResponseModel>> => {
  return axiosInstance.post(`/owners/${ownerId}/pets`, pet, { useV2: false });
};
