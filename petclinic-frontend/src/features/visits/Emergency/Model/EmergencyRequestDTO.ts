import { UrgencyLevel } from './UrgencyLevel';

export interface EmergencyRequestDTO {
  visitDate: Date;

  description: string;

  petName: string;

  urgencyLevel: UrgencyLevel;

  emergencyType: string;
}
