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
  } catch (error) {
    console.error('Failed to update vet photo:', error);
    throw new Error('Failed to update vet photo');
  }
};
