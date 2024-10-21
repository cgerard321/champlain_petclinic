import axiosInstance from '@/shared/api/axiosInstance.ts';
import { VisitResponseModel } from '../models/VisitResponseModel';

export async function GetAllTrueVisits(): Promise<VisitResponseModel[]> {
  const response = await axiosInstance.get('/visits/reminder/true', {
    responseType: 'stream',
  });

  return response.data
    .split('data:')
    .map((dataChunk: string) => {
      try {
        if (dataChunk == '') return null;
        return JSON.parse(dataChunk);
      } catch (err) {
        console.error('Could not parse JSON: ' + err);
      }
    })
    .filter((data?: JSON) => data !== null);
}
