import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export interface TimeSlot {
  startTime: string;
  endTime: string;
  available: boolean;
}

export const getAvailableSlots = async (
  vetId: string,
  date: string
): Promise<AxiosResponse<TimeSlot[]>> => {
  return await axiosInstance.get<TimeSlot[]>(
    `/visits/availability/vets/${vetId}/slots`,
    {
      useV2: false,
      params: { date },
    }
  );
};
