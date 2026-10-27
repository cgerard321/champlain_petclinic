export interface Customer {
  customerId: string;
  firstName: string;
  lastName: string;
  telephone: string;
  address: string;
  city: string;
}
export interface Pet {
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
export interface PetTypeFilters {
  id: string;
  name: string;
  description: string;
}

export function petTypeMatchesFilters(petType: PetType, filters: PetTypeFilters): boolean {
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
    matches(petType.petTypeId, filters.id) &&
    matches(petType.name, filters.name) &&
    matches(petType.petTypeDescription, filters.description)
  );
}
