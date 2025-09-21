import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PhotoResponseModel } from '../models/PhotoResponseModel';

export const getPetPhoto = async (
  petId: string
): Promise<AxiosResponse<PhotoResponseModel>> => {
  return await axiosInstance.get<PhotoResponseModel>(`/pets/${petId}/photo`, {
    useV2: true,
  });
};
