import { Status } from '@/features/visits/models/Status.ts';

export interface VisitRequestModel {
  visitDate: string;
  description: string;
  petId: string;
  practitionerId: string;
  ownerId?: string;
  jwtToken?: string;
  status: Status;
  isEmergency: boolean;
}
