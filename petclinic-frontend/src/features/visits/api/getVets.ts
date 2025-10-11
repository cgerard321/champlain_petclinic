import axiosInstance from '@/shared/api/axiosInstance';

export interface VetResponse {
  vetId: string;
  firstName: string;
  lastName: string;
  active: boolean;
  specialties?: Array<{ specialtyId: string; name: string }>;
}

export const getAvailableVets = async (): Promise<VetResponse[]> => {
  const response = await axiosInstance.get('/vets', {
    responseType: 'text',
    useV2: false,
  });

  const data = response.data;

  if (typeof data === 'string') {
    try {
      const parsed = JSON.parse(data);
      if (Array.isArray(parsed)) {
        return parsed as VetResponse[];
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
      .filter((d: VetResponse | null): d is VetResponse => d !== null);
  }

  if (Array.isArray(data)) {
    return data as VetResponse[];
  }

  return [];
};
