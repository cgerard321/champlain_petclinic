export interface Vet {
  vetId: string;
  vetBillId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: string[];
  workHoursJson?: string;
  active: boolean;
  specialties: Specialty[];
  photoUrl?: string;
  rating?: number;
  count?: number;
  showRating?: boolean;
}

export interface Specialty {
  specialtyId: string;
  name: string;
}

export interface VetRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: string[];
  specialties: Specialty[];
  active: boolean;
}

export interface PaginatedVets {
  vets: Vet[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}


