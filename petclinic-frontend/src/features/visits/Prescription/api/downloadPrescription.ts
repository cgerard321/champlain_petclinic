import axiosInstance from '@/shared/api/axiosInstance';

export const downloadPrescription = async (visitId: string): Promise<Blob> => {
  const response = await axiosInstance.get(
    `/visits/${visitId}/prescriptions/pdf`,
    {
      responseType: 'blob',
      useV2: false,
    }
  );
  return response.data;
};
