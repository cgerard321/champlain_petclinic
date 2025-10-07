import axiosInstance from '@/shared/api/axiosInstance';
import { AxiosResponse } from 'axios';
import { EmergencyRequestDTO } from '../Model/EmergencyRequestDTO';

const toServerDate = (val: string): string => {
  return val.replace('T', ' ').slice(0, 16);
};

export const addEmergency = async (
  emergency: EmergencyRequestDTO
): Promise<AxiosResponse<void>> => {
  const payload = {
    ...emergency,
    visitDate: toServerDate(emergency.visitDate),
  };
  return await axiosInstance.post<void>('/visits/emergencies', payload, {
    useV2: false,
  });
};
