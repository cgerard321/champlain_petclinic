import { Status } from '@/features/visits/models/Status.ts';

export interface VisitResponseModel {
  visitStartDate: string;
  description: string;
  petId: string;
  petName: string;
  petBirthDate: string;
  practitionerId: string;
  vetFirstName: string;
  vetLastName: string;
  vetEmail: string;
  vetPhoneNumber: string;
  status: Status;
  visitId: string;
  visitEndDate: string;
}
