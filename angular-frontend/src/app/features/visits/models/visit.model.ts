export interface Visit {
  visitId: string;
  visitDate: string;
  date: string;
  description: string;
  petId: string;
  petName?: string;
  practitionerId: string;
  vetId: string;
  ownerId: string;
  visitType: string;
  vetFirstName?: string;
  vetLastName?: string;
  vetEmail?: string;
  vetPhoneNumber?: string;
  status: VisitStatus;
}

export enum VisitStatus {
  UPCOMING = 'UPCOMING',
  CONFIRMED = 'CONFIRMED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface VisitRequest {
  visitDate: string;
  description: string;
  petId: string;
  practitionerId: string;
}

export interface PaginatedVisits {
  visits: Visit[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}

export interface Owner {
  ownerId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  pets?: Pet[];
}

export interface Pet {
  petId: string;
  name: string;
  birthDate: string;
  petType: string;
  ownerId: string;
}

export interface Vet {
  vetId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  specialties: Specialty[];
  workday: string[];
  active: boolean;
}

export interface Specialty {
  name: string;
}
