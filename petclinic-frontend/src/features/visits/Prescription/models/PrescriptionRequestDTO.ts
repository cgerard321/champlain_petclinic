import { MedicationDTO } from './MedicationDTO';

export interface PrescriptionRequestDTO {
  prescriptionId?: string;
  date?: string;
  vetFirstName: string;
  vetLastName: string;
  ownerFirstName: string;
  ownerLastName: string;
  petName: string;
  directions: string;
  medications: MedicationDTO[];
}
