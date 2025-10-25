import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '../models/PetResponseModel';

export const deletePetPhoto = async (
  petId: string
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.patch<PetResponseModel>(
    `/pets/${petId}/photo`,
    {},
    {
      useV2: false,
    }
  );
};
