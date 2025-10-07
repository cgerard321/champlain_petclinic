import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const deleteEmergency = async (
  visitEmergencyId: string
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.delete<void>(
    `/visits/emergencies/${visitEmergencyId}`,
    { useV2: false }
  );
};
