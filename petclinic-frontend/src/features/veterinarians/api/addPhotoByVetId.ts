import axiosInstance from '@/shared/api/axiosInstance';

/**
 * Uploads a photo for a specific veterinarian.
 * Uses v1 API endpoint with application/octet-stream content type.
 *
 * @param vetId - The ID of the veterinarian
 * @param photoName - The name for the photo
 * @param file - The image file to upload
 * @throws Error when upload fails
 */
export const addPhotoByVetId = async (
  vetId: string,
  photoName: string,
  file: File
): Promise<void> => {
  try {
    await axiosInstance.post(`/vets/${vetId}/photos/${photoName}`, file, {
      useV2: false,
      headers: {
        'Content-Type': 'application/octet-stream',
      },
    });
  } catch (error) {
    console.error('Error uploading vet photo:', error);
    throw error;
  }
};
