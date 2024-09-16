import { Visit } from '../models/Visit';
import axiosInstance from '@/shared/api/axiosInstance';

export async function getAllVisits(): Promise<Visit[]> {
  const response = await axiosInstance.get<Visit[]>(
    axiosInstance.defaults.baseURL + 'visits',
    {
      headers: {
        Accept: 'text/event-stream',
      },
    }
  );

  if (response.status !== 200) {
    throw new Error(`Error: ${response.status} ${response.statusText}`);
  }

  const data = response.data;
  // eslint-disable-next-line no-console
  console.log('API Response:', data); // Debugging

  return Array.isArray(data) ? data : [data];
}
