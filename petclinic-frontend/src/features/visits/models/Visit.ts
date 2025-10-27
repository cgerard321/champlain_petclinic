import { FileDetails } from '@/shared/models/FileDetails';

export interface Visit {
  visitId: string;
  visitDate: string;
  description: string;
  petId: string;
  petName: string;
  practitionerId: string;
  vetFirstName: string;
  vetLastName: string;
  vetEmail: string;
  vetPhoneNumber: string;
  status: string;
  visitEndDate: string;
  isEmergency: boolean;
  prescriptionFile?: FileDetails;
}
