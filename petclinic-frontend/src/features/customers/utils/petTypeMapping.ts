import { PetTypeModel } from '../models/PetTypeModel';

//mapping for existing pets with numeric IDs from DataSetupService
const legacyPetTypeMapping: { [key: string]: string } = {
  '1': 'Cat',
  '2': 'Dog',
  '3': 'Lizard',
  '4': 'Snake',
  '5': 'Bird',
  '6': 'Hamster',
};

export const createPetTypeMapping = (
  petTypes: PetTypeModel[]
): { [key: string]: string } => {
  const mapping: { [key: string]: string } = {};
  petTypes.forEach(petType => {
    mapping[petType.petTypeId] = petType.name;
  });
  return mapping;
};

export const getPetTypeName = (
  petTypeId: string,
  petTypes: PetTypeModel[]
): string => {
  //first find in API data
  const petType = petTypes.find(pt => pt.petTypeId === petTypeId);
  if (petType) {
    return petType.name;
  }

  //if not fallback to mapping for existing pets
  return legacyPetTypeMapping[petTypeId] || 'Unknown';
};
