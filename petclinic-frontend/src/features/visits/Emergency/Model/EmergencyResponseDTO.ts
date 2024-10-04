import { UrgencyLevel } from './UrgencyLevel';

export interface EmergencyResponseDTO {
  visitEmergencyId: string;
  visitDate: Date;

  description: string;

  petName: string;

  urgencyLevel: UrgencyLevel;

  emergencyType: string;
}
