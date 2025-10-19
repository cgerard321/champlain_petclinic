import axiosInstance from '@/shared/api/axiosInstance';

export const downloadPrescription = async (
  visitId: string,
  onProgress?: (percent: number) => void
): Promise<Blob> => {
  const response = await axiosInstance.get(
    `/visits/${visitId}/prescriptions/pdf`,
    {
      responseType: 'blob',
      useV2: false,
      onDownloadProgress: e => {
        if (onProgress && e.total) {
          onProgress(Math.round((e.loaded / e.total) * 100));
        }
      },
    }
  );
  return response.data;
};
