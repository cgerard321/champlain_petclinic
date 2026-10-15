import { CancellationReason } from './CancellationReasons';
import { FileDetails } from './FileDetails';
import { Status } from './Status';

export interface VisitResponseModel {
  visitId: string;
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
  isEmergency: boolean;
  ownerFirstName: string;
  ownerLastName: string;
  prescriptionFile?: FileDetails;
}
