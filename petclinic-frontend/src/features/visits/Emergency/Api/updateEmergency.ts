import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyRequestDTO } from '../Model/EmergencyRequestDTO';

// This converts "YYYY-MM-DDTHH:mm" to "YYYY-MM-DD HH:mm"
const toServerDate = (val: string): string => {
  return val.replace('T', ' ').slice(0, 16);
};

export const updateEmergency = async (
  emergencyVisitId: string,
  emergency: EmergencyRequestDTO
): Promise<void> => {
  await axiosInstance.put<void>(
    `/visits/emergencies/${emergencyVisitId}`,
    {
      ...emergency,
      visitDate: toServerDate(emergency.visitDate),
    },
    { useV2: false }
  );
};
