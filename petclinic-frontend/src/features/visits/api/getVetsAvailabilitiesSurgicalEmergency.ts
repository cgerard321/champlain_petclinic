import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import EmergencyAvailability from '../models/EmergencyResponse';

export const getVetsAvailabilitiesSurgicalEmergency = async (): Promise<
  AxiosResponse<EmergencyAvailability[]>
> => {
  return await axiosInstance.get<EmergencyAvailability[]>(
    `/visits/availability/emergency/surgical`,
    {
      useV2: false,
    }
  );
};
