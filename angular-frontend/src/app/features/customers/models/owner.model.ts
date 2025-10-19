import { Pet } from './pet.model';

export interface Owner {
  ownerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
  pets?: Pet[];
}

export interface OwnerRequest {
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
}

export interface PaginatedOwners {
  owners: Owner[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}


