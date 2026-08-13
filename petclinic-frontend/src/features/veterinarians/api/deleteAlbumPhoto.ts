import axiosInstance from '@/shared/api/axiosInstance';

export const deleteAlbumPhoto = async (
  vetId: string,
  photoId: number
): Promise<void> => {
  try {
    await axiosInstance.delete(`/vets/${vetId}/albums/${photoId}`, {
      useV2: true,
    });
  } catch (error) {
    console.error('Failed to delete album photo:', error);
    throw new Error('Failed to delete album photo');
  }
};
