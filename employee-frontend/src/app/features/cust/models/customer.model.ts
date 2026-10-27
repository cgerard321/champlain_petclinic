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
  petTypeDescription: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  telephone: string;
  address: string;
  city: string;
}

export interface PetRequest {
  name: string;
  birthDate: Date;
  petTypeId: string;
  weight: string;
}
export interface PetFilters {
  petType: string;
  name: string;
}

export function petMatchesFilters(pet: Pet, petTypeNames: string[], filters: PetFilters): boolean {
  const matches = (value: string | null | undefined, filter: string): boolean => {
    const query = filter.trim().toLowerCase();
    return (
      query === '' ||
      String(value ?? '')
        .toLowerCase()
        .includes(query)
    );
  };

  return (
    petTypeNames.some((typeName) => matches(typeName, filters.petType)) &&
    matches(pet.name, filters.name)
  );
}
