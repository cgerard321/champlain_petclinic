import axiosInstance from '@/shared/api/axiosInstance';

export async function deleteVetRating(vetId: string): Promise<void> {
  try {
    await axiosInstance.delete(`/vets/${vetId}/ratings/customer`, {
      useV2: false,
    });
  } catch (error) {
    console.error('Error deleting vet rating:', error);
    throw error;
  }
}
