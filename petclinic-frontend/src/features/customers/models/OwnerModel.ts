import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';

export interface OwnerModel {
  ownerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
  pets: PetResponseModel[];
}
