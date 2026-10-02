import { FileDetails } from '@/shared/models/FileDetails';
import { CancellationReason } from '@/features/visits/models/CancellationReason.ts';

export interface Visit {
  visitId: string;
  visitDate: string;
  description: string;
  petId: string;
  petName: string;
  vetFirstName: string;
  vetLastName: string;
  vetEmail: string;
  vetPhoneNumber: string;
  status: string;
  cancellationReason?: CancellationReason;
  cancellationReasonDetails?: string;
  visitEndDate: string;
  isEmergency: boolean;
  prescriptionFile?: FileDetails;
}
