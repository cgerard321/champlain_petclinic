import { UrgencyLevel } from './UrgencyLevel';

export interface EmergencyRequestDTO {
  visitDate: Date;

  description: string;

  petId: string;

  practitionerId: string;

  petName: string;

  urgencyLevel: UrgencyLevel;

  emergencyType: string;
}
