import axiosInstance from '@/shared/api/axiosInstance';
import { AxiosResponse } from 'axios';
import { EmergencyRequestDTO } from '../Model/EmergencyRequestDTO';

export const addEmergency = async (
  emergency: EmergencyRequestDTO
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>('/visits/emergency', emergency, {
    useV2: false,
  });
};
