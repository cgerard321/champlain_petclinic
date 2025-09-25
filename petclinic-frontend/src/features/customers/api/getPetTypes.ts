import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';

export const getPetTypes = async (): Promise<AxiosResponse<PetTypeModel[]>> => {
  return axiosInstance.get('/owners/petTypes', { useV2: false });
};
