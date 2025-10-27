import axiosInstance from '@/shared/api/axiosInstance.ts';
import { VisitResponseModel } from '../models/VisitResponseModel';

export async function getAllVisits(): Promise<VisitResponseModel[]> {
  try {
    const response = await axiosInstance.get('/visits', {
      responseType: 'stream',
      useV2: false,
    });

    if (typeof response.data !== 'string') {
      console.error('Expected string response, got:', typeof response.data);
      return [];
    }

    return response.data
      .split('data:')
      .map((payload: string): VisitResponseModel | null => {
        try {
          const trimmed = payload.trim();
          if (trimmed === '') return null;

          return JSON.parse(trimmed) as VisitResponseModel;
        } catch (err) {
          console.error("Can't parse JSON:", err);
          return null;
        }
      })
      .filter(
        (data: VisitResponseModel | null): data is VisitResponseModel =>
          data !== null
      );
  } catch (error) {
    console.error('Error fetching visits:', error);
    throw error;
  }
}
