import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';

export const deletePet = async (
  ownerId: string,
  petId: string
): Promise<AxiosResponse<PetResponseModel>> => {
  return await axiosInstance.delete<PetResponseModel>(
    `/owners/${ownerId}/pets/${petId}`,
    {
      useV2: true,
    }
  );
};
