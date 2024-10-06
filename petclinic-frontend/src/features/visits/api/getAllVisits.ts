import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';

export async function getAllVisits(searchTerm: string): Promise<Visit[]> {
  const params: Record<string, string> = {};
  if (searchTerm !== '') params.searchTerm = searchTerm;
  const res = await axiosInstance.get('/visits', {
    responseType: 'stream',
    params,
  });

  return res.data
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
