import { PetResponseModel } from './petsResponseModel';
//import { FileDetails } from '@/shared/models/FileDetails';

export interface CustomerResponseModel {
  customerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
  pets: PetResponseModel[];
  //photo?: FileDetails;
}
