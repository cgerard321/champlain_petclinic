import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyRequestDTO } from '../Model/EmergencyRequestDTO';
import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';

export const updateEmergency = async (
  visitEmergencyId: string,
  emergency: EmergencyRequestDTO
): Promise<void> => {
  await axiosInstance.put<void>(
    `/visits/emergency/${visitEmergencyId}`,
    emergency
  );
};

export const getEmergency = async (
  visitEmergencyId: string
): Promise<EmergencyResponseDTO> => {
  const response = await axiosInstance.get<EmergencyResponseDTO>(
    `/visits/emergency/${visitEmergencyId}`
  );
  return response.data; // Return only the data
};
