import axiosInstance from '@/shared/api/axiosInstance.ts';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

export async function getAllVets(): Promise<VetResponseModel[]> {
  const response = await axiosInstance.get('/vets', {
    responseType: 'text',
    useV2: true,
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
