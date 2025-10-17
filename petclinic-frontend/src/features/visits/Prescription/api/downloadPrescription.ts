import axiosInstance from '@/shared/api/axiosInstance';

export const downloadPrescription = async (
  visitId: string,
  prescriptionId: string
): Promise<Blob> => {
  const response = await axiosInstance.get(
    `/visits/${visitId}/prescriptions/${prescriptionId}/pdf`,
    {
      responseType: 'blob',
      useV2: false,
    }
  );
  return response.data;
};
