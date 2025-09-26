import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const deleteEmergency = async (
  visitEmergencyId: string
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.delete<void>(
    `/visits/emergency/${visitEmergencyId}`,
    { useV2: true }
  );
};
