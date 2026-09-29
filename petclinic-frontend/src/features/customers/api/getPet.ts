import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel.ts';
import { AxiosResponse } from 'axios';

export const getPet = async (
  petId: string,
  customerId?: string
): Promise<AxiosResponse<PetResponseModel>> => {
  if (customerId) {
    return await axiosInstance.get<PetResponseModel>(
      `/pets/customers/${customerId}/pets/${petId}`,
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
