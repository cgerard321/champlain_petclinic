import axiosInstance from '@/shared/api/axiosInstance';

export const downloadPrescription = async (visitId: string): Promise<Blob> => {
  const response = await axiosInstance.get(
    `/visits/${visitId}/prescription/pdf`,
    {
      responseType: 'arraybuffer',
      useV2: false,
    }
  );
  return new Blob([response.data], { type: 'application/pdf' });
};
