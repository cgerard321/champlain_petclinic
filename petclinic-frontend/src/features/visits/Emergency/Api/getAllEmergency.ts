import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';

export async function getAllEmergency(): Promise<EmergencyResponseDTO[]> {
  const response = await axiosInstance.get(`/visits/emergency`, {
    // responseType: 'stream',
    useV2: true,
  });
  return response.data // Return only the data
    .split('data:')
    .map((payLoad: string) => {
      try {
        if (payLoad == '') return null;
        return JSON.parse(payLoad);
      } catch (err) {
        console.error("Can't parse JSON: " + err);
      }
    })
    .filter((data?: JSON) => data !== null);
}
