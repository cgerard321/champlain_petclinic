import { PetTypeModel } from '../models/PetTypeModel';
import snakeImg from '@/assets/Owners/snake.png';
import catImg from '@/assets/Owners/cat.png';
import dogImg from '@/assets/Owners/dog.png';
import lizardImg from '@/assets/Owners/lizard.png';
import hamsterImg from '@/assets/Owners/hamster.png';
import birdImg from '@/assets/Owners/bird.png';
import othersImg from '@/assets/Owners/others.png';

//mapping for existing pets with numeric IDs from DataSetupService
const legacyPetTypeMapping: { [key: string]: string } = {
  '1': 'Cat',
  '2': 'Dog',
  '3': 'Lizard',
  '4': 'Snake',
  '5': 'Bird',
  '6': 'Hamster',
  '7': 'Others',
};

const petTypeImageMapping: { [key: string]: string } = {
  Cat: catImg,
  Dog: dogImg,
  Lizard: lizardImg,
  Snake: snakeImg,
  Bird: birdImg,
  Hamster: hamsterImg,
  Others: othersImg,
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

export const getPetTypeImage = (
  petTypeId: string,
  petTypes: PetTypeModel[]
): string => {
  const petTypeName = getPetTypeName(petTypeId, petTypes);
  return petTypeImageMapping[petTypeName] || othersImg;
};
