import axiosInstance from '@/shared/api/axiosInstance';

export const deleteSpecialty = async (
  vetId: string,
  specialtyId: string
): Promise<void> => {
  try {
    await axiosInstance.delete(`/vets/${vetId}/specialties/${specialtyId}`, {
      useV2: true,
    });
  } catch (error) {
    console.error('Failed to delete specialty:', error);
    throw new Error('Failed to delete specialty');
  }
};
