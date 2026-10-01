import axiosInstance from '@/shared/api/axiosInstance';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

export const getAvailableVets = async (): Promise<VetResponseModel[]> => {
  const response = await axiosInstance.get('/vets', {
    responseType: 'text',
    useV2: false,
  });

  const data = response.data;

  if (typeof data === 'string') {
    try {
      const parsed = JSON.parse(data);
      if (Array.isArray(parsed)) {
        return parsed as VetResponseModel[];
      }
    } catch (err) {}

    return response.data
      .split('data:')
      .map((payLoad: string) => {
        try {
          if (payLoad.trim() === '') return null;
          return JSON.parse(payLoad);
        } catch (err) {
          console.error('Cannot parse vet payload:', err);
          return null;
        }
      })
      .filter(
        (d: VetResponseModel | null): d is VetResponseModel => d !== null
      );
  }

  if (Array.isArray(data)) {
    return data as VetResponseModel[];
  }

  return [];
};
