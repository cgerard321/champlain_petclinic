import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel.ts';
import { PetRequestModel } from '@/features/customers/models/PetRequestModel.ts';

export const updatePet = async (
  petId: string,
  pet: PetRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(
    `http://localhost:8080/api/v2/gateway/pets/${petId}`,
    pet
  );
};

export const getPet = async (
  petId: string
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.get<PetResponseModel>(
    `http://localhost:8080/api/v2/gateway/pets/${petId}`
  );
};
