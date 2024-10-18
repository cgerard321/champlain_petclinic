import { UrgencyLevel } from './UrgencyLevel';

export interface EmergencyResponseDTO {
  visitEmergencyId: string;
  visitDate: Date;

  description: string;

  petId: string;
  petName: string;
  vetBirthDate: Date;
  practitionerId: string;
  vetFirstName: string;
  vetLastName: string;
  vetEmail: string;
  vetPhoneNumber: string;

  urgencyLevel: UrgencyLevel;

  emergencyType: string;
}
