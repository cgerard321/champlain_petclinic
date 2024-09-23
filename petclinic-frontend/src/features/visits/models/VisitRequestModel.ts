import { Status } from '@/features/visits/models/Status.ts';

export interface VisitRequestModel {
  visitStartDate: string;
  description: string;
  petId: string;
  practitionerId: string;
  // ownerId: string;
  status: Status;
}
