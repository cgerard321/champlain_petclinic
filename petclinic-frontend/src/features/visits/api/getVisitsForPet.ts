import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';

export async function getVisitsForPet(petId: string): Promise<Visit[]> {
  try {
    const response = await axiosInstance.get(`/visits/pets/${petId}`, {
      responseType: 'text',
      useV2: false,
    });

    return response.data
      .split('data:')
      .map((payload: string) => {
        try {
          if (payload === '') return null;
          return JSON.parse(payload);
        } catch (err) {
          console.error("Can't parse JSON:", err);
          return null;
        }
      })
      .filter((data: Visit | null) => data !== null);
  } catch (error) {
    console.error('Error fetching visits for pet:', error);
    throw error;
  }
}
