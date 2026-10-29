export type Workday =
  'Monday' | 'Tuesday' | 'Wednesday' | 'Thursday' | 'Friday' | 'Saturday' | 'Sunday';

export interface VetSpecialty {
  specialtyId: string;
  name: string;
}

export interface VetEducation {
  vetId: string;
  schoolName: string;
  degree: string;
  fieldOfStudy: string;
  startDate: string;
  endDate: string;
}

export interface VetEducationResponse extends VetEducation {
  educationId: string;
}

export interface VetPhoto {
  fileId: string;
  fileName: string;
  fileType: string;
  fileData: string;
}

export interface VetResponse {
  vetId: string;
  vetBillId: string | null;
  firstName: string;
  lastName: string;
  email: string | null;
  phoneNumber: string | null;
  resume: string | null;
  workday: Workday[] | null;
  workHoursJson: string | null;
  active: boolean;
  specialties: VetSpecialty[] | null;
  photo?: VetPhoto | null;
}

export interface VetUpdateRequest {
  vetId: string;
  vetBillId: string | null;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: Workday[];
  workHoursJson: string | null;
  active: boolean;
  specialties: VetSpecialty[];
  photoDefault: boolean;
}

export interface VetEditSubmission {
  request: VetUpdateRequest;
  newSpecialtyNames: string[];
  newEducation: VetEducation[];
}
