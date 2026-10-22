import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const getAvailableDates = async (
  vetId: string,
  startDate: string,
  endDate: string
): Promise<AxiosResponse<string[]>> => {
  return await axiosInstance.get<string[]>(
    `/visits/availability/vets/${vetId}/dates`,
    {
      useV2: false,
      params: { startDate, endDate },
    }
  );
};
