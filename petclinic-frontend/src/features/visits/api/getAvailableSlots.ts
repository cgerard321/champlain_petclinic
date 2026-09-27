import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

//TODO We do not yet have a timeslot frontend model, would be cleaner to add one
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
    //   sends a get request to retrieve time slots for the asked vet id

    `/visits/availability/vets/${vetId}/slots`,
    {
      useV2: false,
      params: { date },
    }
  );
};
