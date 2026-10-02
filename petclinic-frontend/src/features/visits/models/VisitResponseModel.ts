import { Status } from '@/features/visits/models/Status.ts';
import { FileDetails } from '@/shared/models/FileDetails';
import { CancellationReason } from '@/features/visits/models/CancellationReason.ts';

export interface VisitResponseModel {
  visitDate: string;
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
  cancellationReason?: CancellationReason;
  cancellationReasonDetails?: string;
  visitId: string;
  visitEndDate: string;
  isEmergency: boolean;
  ownerFirstName: string;
  ownerLastName: string;
  prescriptionFile?: FileDetails;
}
