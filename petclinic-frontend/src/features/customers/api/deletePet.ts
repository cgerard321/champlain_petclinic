
import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';

export const deletePet = async (
  petId: string
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.delete<PetResponseModel>(`/pets/${petId}`);
};
