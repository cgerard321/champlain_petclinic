export interface Pet {
  petId: string;
  name: string;
  birthDate: string;
  petTypeId: string;
  ownerId: string;
  isActive: boolean;
  weight?: number;
  photoUrl?: string;
}

export interface PetType {
  petTypeId: string;
  name: string;
  petTypeDescription?: string;
}

export interface PetRequest {
  name: string;
  birthDate: string;
  petTypeId: string;
  weight?: number;
}
