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
