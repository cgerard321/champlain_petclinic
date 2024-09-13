import { PetResponseModel } from './PetResponseModel.ts';

export interface OwnerResponseModel {
  ownerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
  pets: PetResponseModel[];
}
