import axiosInstance from '@/shared/api/axiosInstance';

/**
 * Fetch a vet's photo as a Blob URL that you can set on an <img src=...>.
 * Falls back to the default image if the request fails.
 */
export const fetchVetPhoto = async (vetId: string): Promise<string> => {
  try {
    const response = await axiosInstance.get(`/vets/${vetId}/photo`, {
      responseType: 'blob',
      useV2: true,
    });

    const blob = response.data as Blob;
    return URL.createObjectURL(blob);
  } catch (error) {
    console.error('Failed to fetch vet photo:', error);
    return '/images/vet_default.jpg';
  }
};
