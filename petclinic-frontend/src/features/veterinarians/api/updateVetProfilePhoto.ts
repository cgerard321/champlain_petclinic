import axiosInstance from '@/shared/api/axiosInstance';

export const updateVetProfilePhoto = async (
  vetId: string,
  file: File
): Promise<Blob> => {
  try {
    const response = await axiosInstance.put(
      `/vets/${vetId}/photo/${encodeURIComponent(file.name)}`,
      file,
      {
        params: { useV2: false },
        headers: {
          'Content-Type': file.type || 'application/octet-stream',
          Accept: 'image/*',
        },
        responseType: 'blob',
      }
    );
    return response.data;
  } catch (error: any) {
      const errorMessage =
          error?.response?.data?.message || 'Failed to update vet photo. Please try again.';
      console.error(errorMessage, error);
      throw new Error(errorMessage);
  }
};
