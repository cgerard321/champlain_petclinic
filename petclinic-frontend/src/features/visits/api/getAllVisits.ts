import axiosInstance from '@/shared/api/axiosInstance.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';

export async function getAllVisits(searchTerm?: string): Promise<VisitResponseModel[]> {
  try {
    const params: Record<string, string> = {};
    if (searchTerm && searchTerm !== '') {
      params.searchTerm = searchTerm;
    }
    
    const response = await axiosInstance.get('/visits', {
      responseType: 'text',
      useV2: false,
      params,
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
