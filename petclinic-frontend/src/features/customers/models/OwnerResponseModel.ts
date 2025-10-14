import { PetResponseModel } from './PetResponseModel.ts';
import { FileDetails } from '@/shared/models/FileDetails';

export interface OwnerResponseModel {
  ownerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
  pets: PetResponseModel[];
  photoId?: string;
  photo?: FileDetails;
}
