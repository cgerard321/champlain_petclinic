import { ProfilePhoto } from '@features/cust/models/profile-photo.model';

import { PetResponseModel } from './petsResponseModel';

export interface CustomerResponseModel {
  customerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
  pets: PetResponseModel[];
  photo?: ProfilePhoto | null;
}
