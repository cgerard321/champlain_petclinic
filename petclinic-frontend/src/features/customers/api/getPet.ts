import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel.ts';
import { AxiosResponse } from 'axios';

export const getPet = async (
  petId: string,
  ownerId?: string
): Promise<AxiosResponse<PetResponseModel>> => {
  if (ownerId) {
    return await axiosInstance.get<PetResponseModel>(
      `/pets/owners/${ownerId}/pets/${petId}`,
      {
        useV2: false,
        params: { includePhoto: true },
      }
    );
  } else {
    return await axiosInstance.get<PetResponseModel>(`/pets/${petId}`, {
      useV2: false,
      params: { includePhoto: true },
    });
  }
};
