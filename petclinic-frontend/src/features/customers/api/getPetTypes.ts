import axiosInstance from '@/shared/api/axiosInstance';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';

export const getPetTypes = async (): Promise<PetTypeModel[]> => {
  const response = await axiosInstance.get('/owners/petTypes', {
    useV2: false,
  });

  if (typeof response.data === 'string') {
    const pieces = response.data.split('\n').filter(Boolean);
    const petTypes: PetTypeModel[] = [];
    for (const piece of pieces) {
      if (piece.startsWith('data:')) {
        const petTypeData = piece.slice(5).trim();
        try {
          const petType: PetTypeModel = JSON.parse(petTypeData);
          petTypes.push(petType);
        } catch (parseError) {
          console.error('Error parsing pet type data:', parseError);
        }
      }
    }
    return petTypes;
  } else if (Array.isArray(response.data)) {
    return response.data;
  } else {
    return [];
  }
};
