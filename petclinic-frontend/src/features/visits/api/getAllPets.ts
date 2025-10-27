import axiosInstance from '@/shared/api/axiosInstance';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';

export const getAllPets = async (
  ownerId?: string
): Promise<PetResponseModel[]> => {
  try {
    const endpoint = ownerId ? `/owners/${ownerId}/pets` : '/pets';

    const response = await axiosInstance.get(endpoint, {
      useV2: false,
      responseType: 'text',
    });

    if (typeof response.data === 'string') {
      const pieces = response.data.split('\n').filter(Boolean);
      const pets: PetResponseModel[] = [];

      for (const piece of pieces) {
        if (piece.startsWith('data:')) {
          const petData = piece.slice(5).trim();
          try {
            const pet: PetResponseModel = JSON.parse(petData);
            pets.push(pet);
          } catch (parseError) {
            console.error('Error parsing pet data:', parseError);
          }
        }
      }
      return pets;
    } else if (Array.isArray(response.data)) {
      return response.data;
    } else {
      return [];
    }
  } catch (error) {
    console.error('Error fetching all pets:', error);
    throw error;
  }
};
