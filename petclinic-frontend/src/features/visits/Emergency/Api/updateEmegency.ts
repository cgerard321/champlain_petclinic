import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyRequestDTO } from '../Model/EmergencyRequestDTO';

export const updateEmergency = async (
  emergencyVisitId: string,
  emergency: EmergencyRequestDTO
): Promise<void> => {
  await axiosInstance.put<void>(
    `/visits/emergency/${emergencyVisitId}`,
    emergency,
    { useV2: false }
  );
};
