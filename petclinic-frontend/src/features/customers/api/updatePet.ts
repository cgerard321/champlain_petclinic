import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetRequestModel } from '@/features/customers/models/PetRequestModel.ts';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';

export const updatePet = async (
  petId: string,
  pet: PetRequestModel
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.put<PetResponseModel>(`/pets/${petId}`, pet, {
    useV2: false,
  });
};
