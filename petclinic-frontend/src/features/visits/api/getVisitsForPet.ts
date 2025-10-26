import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';

export async function getVisitsForPet(petId: string): Promise<Visit[]> {
  try {
    const cleanPetId = petId.trim();
    const response = await axiosInstance.get(`/visits/pets/${cleanPetId}`, {
      responseType: 'text',
      useV2: false,
    });

    if (typeof response.data !== 'string') {
      console.error('Expected string response, got:', typeof response.data);
      return [];
    }

    return response.data
      .split('data:')
      .map((payload: string): Visit | null => {
        try {
          const trimmed = payload.trim();
          if (trimmed === '') return null;

          return JSON.parse(trimmed) as Visit;
        } catch (err) {
          console.error("Can't parse JSON:", err);
          return null;
        }
      })
      .filter((data: Visit | null): data is Visit => data !== null);
  } catch (error) {
    console.error('Error fetching visits for pet:', error);
    throw error;
  }
}
