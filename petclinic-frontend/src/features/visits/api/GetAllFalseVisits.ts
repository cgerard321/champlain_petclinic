import axiosInstance from '@/shared/api/axiosInstance.ts';
import { VisitResponseModel } from '../models/VisitResponseModel';

export async function GetAllFalseVisits(): Promise<VisitResponseModel[]> {
  const response = await axiosInstance.get('/visits/reminder/false', {
    responseType: 'stream',
  });
  // eslint-disable-next-line no-console
  console.log(response.data);
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
