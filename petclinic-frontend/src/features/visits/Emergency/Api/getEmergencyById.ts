import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';

export const getEmergencyById = async (
  visitEmergencyId: string
): Promise<EmergencyResponseDTO> => {
  const response = await axiosInstance.get<EmergencyResponseDTO>(
    `http://localhost:8080/api/v2/gateway/visits/emergency/${visitEmergencyId}`
  );
  return response.data; // Return only the data
};
