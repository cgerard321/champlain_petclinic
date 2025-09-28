import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel.ts';

export const getPet = async (
  ownerId: string,
  petId: string
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.get<PetResponseModel>(
    `/owners/${ownerId}/pets/${petId}`,
    {
      useV2: true,
    }
  );
};
