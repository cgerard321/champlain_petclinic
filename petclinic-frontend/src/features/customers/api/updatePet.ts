import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetRequestModel } from '@/features/customers/models/PetRequestModel.ts';

export const updatePet = async (
  ownerId: string,
  petId: string,
  pet: PetRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(
    `/owners/${ownerId}/pets/${petId}`,
    pet,
    { useV2: true }
  );
};
