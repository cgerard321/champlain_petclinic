import axiosInstance from '@/shared/api/axiosInstance';

export const deleteVetRating = async (vetId: string): Promise<void> => {
  try {
    await axiosInstance.delete(`/vets/${vetId}/ratings/customer`, {
      useV2: true,
    });
  } catch (error) {
    console.error('Failed to delete vet rating:', error);
    throw new Error('Failed to delete vet rating');
  }
};
