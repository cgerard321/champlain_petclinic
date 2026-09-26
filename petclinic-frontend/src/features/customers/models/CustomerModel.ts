import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';

export interface CustomerModel {
  customerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
  pets: PetResponseModel[];
}
