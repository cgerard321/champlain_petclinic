import axiosInstance from '@/shared/api/axiosInstance.ts';
import { VisitResponseModel } from '../models/VisitResponseModel';

export async function getAllOwnerVisits(
  ownerId: string
): Promise<VisitResponseModel[]> {
  try {
    const cleanOwnerId = ownerId.trim();
    const response = await axiosInstance.get(
      `/visits/owners/${cleanOwnerId}/visits`,
      {
        responseType: 'text',
        useV2: false,
      }
    );
    if (typeof response.data !== 'string') {
      console.error('Expected string response, got:', typeof response.data);
      return [];
    }
    return response.data
      .split('data:')
      .map((dataChunk: string): VisitResponseModel | null => {
        try {
          const trimmed = dataChunk.trim();
          if (trimmed === '') return null;
          return JSON.parse(trimmed) as VisitResponseModel;
        } catch (err) {
          console.error('Could not parse JSON: ' + err);
          return null;
        }
      })
      .filter(
        (data: VisitResponseModel | null): data is VisitResponseModel =>
          data !== null
      );
  } catch (error) {
    console.error('Error fetching owner visits:', error);
    throw error;
  }
}
