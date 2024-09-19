import { Status } from '@/features/visits/models/Status.ts';

export interface VisitRequestModel {
    visitDate: Date;
    description: string;
    petId: string;
    practitionerId: string;
    // ownerId: string;
    status: Status;
}