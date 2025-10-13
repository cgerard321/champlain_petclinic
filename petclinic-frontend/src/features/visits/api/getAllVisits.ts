import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';

export async function getAllVisits(searchTerm: string = ''): Promise<Visit[]> {
  try {
    const params: Record<string, string> = {};
    if (searchTerm !== '') params.searchTerm = searchTerm;

    const response = await axiosInstance.get('/visits', {
      responseType: 'text',
      params,
      useV2: false,
    });
    return response.data
      .split('data:')
      .map((payload: string) => {
        try {
          if (payload == '') return null;
          return JSON.parse(payload);
        } catch (err) {
          console.error("Can't parse JSON:", err);
        }
      })
      .filter((data?: JSON) => data !== null);
  } catch (error) {
    console.error('Error fetching visits:', error);
    throw error;
  }
}
