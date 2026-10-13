import { CancellationReason } from './CancellationReasons';
import { FileDetails } from './FileDetails';
import { Status } from './Status';

export interface Visit {
  visitId: string;
  visitDate: Date;
  description: string;
  petId: string;
  petName: string;
  petBirthDate: Date;
  vetFirstName: string;
  vetLastName: string;
  vetEmail: string;
  vetPhoneNumber: string;
  practitionerId: string;
  status: Status;
  cancellationReason?: CancellationReason;
  cancellationReasonDetails?: string;
  visitEndDate: Date;
  isEmergency: boolean;
  prescriptionFile?: FileDetails;
}
