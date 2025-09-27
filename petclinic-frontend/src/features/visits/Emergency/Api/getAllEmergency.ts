import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';

export async function getAllEmergency(): Promise<EmergencyResponseDTO[]> {
  const response = await axiosInstance.get(`/visits/emergency`, {
    // responseType: 'stream',
    useV2: true,
  });
  return response.data;
}
