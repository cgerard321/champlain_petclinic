import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const getEmergencyVisitsByOwnerId = async (
  ownerId: string
): Promise<EmergencyResponseDTO[]> => {
  try {
    const response = await axiosInstance.get<string>(
      `/visits/owners/${ownerId}/emergencies`,
      {
        useV2: false,
        responseType: 'text',
      }
    );

    return response.data
      .split('data:')
      .map((dataChunk: string): EmergencyResponseDTO | null => {
        try {
          if (dataChunk.trim() === '') return null;
          return JSON.parse(dataChunk) as EmergencyResponseDTO;
        } catch (err) {
          console.error('Could not parse JSON:', err);
          return null;
        }
      })
      .filter((data): data is EmergencyResponseDTO => data !== null);
  } catch (error) {
    console.error('Error fetching emergency visits:', error);
    throw error;
  }
};
