import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel';

export const deletePetPhoto = async (
  ownerId: string,
  petId: string
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.patch<PetResponseModel>(
    `/owners/${ownerId}/pets/${petId}/photo`,
    {},
    {
      useV2: false,
    }
  );
};
