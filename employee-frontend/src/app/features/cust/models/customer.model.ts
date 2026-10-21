import { ProfilePhoto } from '@features/cust/models/profile-photo.model';

export interface Customer {
  customerId: string;
  firstName: string;
  lastName: string;
  telephone: string;
  address: string;
  city: string;
}
export interface Pet {
  photo?: ProfilePhoto | null;
  customerId: string;
  petId: string;
  name: string;
  birthDate: Date;
  petTypeId: string;
  isActive: string;
  weight: string;
}
export interface PetType {
  petTypeId: string;
  name: string;
}
