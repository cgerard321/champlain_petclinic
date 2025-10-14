import axiosInstance from '@/shared/api/axiosInstance.ts';
import { VisitResponseModel } from '../models/VisitResponseModel';

export async function getAllVisits(): Promise<VisitResponseModel[]> {
  try {
    const response = await axiosInstance.get('/visits', {
      responseType: 'stream',
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
          return null;
        }
      })
      .filter((data?: Visit | null) => data !== null);
  } catch (error) {
    console.error('Error fetching visits:', error);
    throw error;
  }
}
