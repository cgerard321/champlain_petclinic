import axios, { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { VetRequestModel } from '../models/VetRequestModel';
import { VetResponseModel } from '../models/VetResponseModel';

export const updateVet = async (
  vetId: string,
  vet: VetRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.put<void>(`/vets/${vetId}`, vet);
};

export const getVet = async (
  userId: string
): Promise<AxiosResponse<VetResponseModel>> => {
  return await axios.get<VetResponseModel>(
    `http://localhost:8080/api/gateway/vet/${userId}`
  );
};
