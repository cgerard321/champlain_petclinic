import axiosInstance from '@/shared/api/axiosInstance';

export interface SpecialtyDTO {
  specialtyId: string;
  name: string;
}

export const addSpecialty = async (
  vetId: string,
  specialty: SpecialtyDTO
): Promise<void> => {
  try {
    await axiosInstance.post(`/vets/${vetId}/specialties`, specialty, {
      useV2: true,
    });
  } catch (error) {
    console.error('Failed to add specialty:', error);
    throw new Error('Failed to add specialty');
  }
};
