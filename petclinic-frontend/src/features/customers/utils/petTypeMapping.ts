import { PetTypeModel } from '../models/PetTypeModel';
import snakeImg from '@/assets/Customers/snake.png';
import catImg from '@/assets/Customers/cat.png';
import dogImg from '@/assets/Customers/dog.png';
import lizardImg from '@/assets/Customers/lizard.png';
import hamsterImg from '@/assets/Customers/hamster.png';
import birdImg from '@/assets/Customers/bird.png';
import othersImg from '@/assets/Customers/others.png';
import { TFunction } from 'i18next';

const legacyPetTypeMapping: { [key: string]: string } = {
  '1': 'cat',
  '2': 'dog',
  '3': 'lizard',
  '4': 'snake',
  '5': 'bird',
  '6': 'hamster',
  '7': 'others',
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
  petTypes: PetTypeModel[],
  t: TFunction
): string => {
  // First find in API data
  const petType = petTypes.find(pt => pt.petTypeId === petTypeId);

  if (petType) {
    return t(`pets.types.${petType.name.toLowerCase()}`);
  }

  // Fallback for existing pets with legacy numeric IDs
  const legacyPetType = legacyPetTypeMapping[petTypeId];

  if (legacyPetType) {
    return t(`pets.types.${legacyPetType}`);
  }

  return t('pets.types.unknown');
};

export const getPetTypeImage = (
  petTypeId: string,
  petTypes: PetTypeModel[],
  t: TFunction
): string => {
  const petTypeName = getPetTypeName(petTypeId, petTypes, t);

  return petTypeImageMapping[petTypeName] || othersImg;
};
