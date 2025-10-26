import axiosInstance from '@/shared/api/axiosInstance.ts';
import { OwnerResponseModel } from '../models/OwnerResponseModel';

export async function getAllOwners(): Promise<OwnerResponseModel[]> {
  const response = await axiosInstance.get('/owners', {
    responseType: 'text',
    useV2: false,
  });
  return response.data
    .split('data:')
    .map((payLoad: string) => {
      try {
        if (payLoad == '') return null;
        return JSON.parse(payLoad);
      } catch (err) {
        console.error("Can't parse JSON: " + err);
      }
    })
    .filter((data?: JSON) => data !== null);
}
