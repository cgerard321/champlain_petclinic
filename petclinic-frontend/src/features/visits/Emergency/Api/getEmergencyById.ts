import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';

export const getEmergencyById = async (
  visitEmergencyId: string
): Promise<EmergencyResponseDTO> => {
  const response = await axiosInstance.get<EmergencyResponseDTO>(
    `/visits/emergencies/${visitEmergencyId}`,
    { useV2: false }
  );
  return response.data; // Return only the data
};
