import { Status } from '@/features/visits/models/Status.ts';

export interface VisitResponseModel {
    visitDate: Date;
    description: string;
    petId: string;
    petName: string;
    petBirthDate: Date;
    practitionerId: string;
    vetFirstName: string;
    vetLastName: string;
    vetEmail: string;
    vetPhoneNumber: string;
    status: Status;
    visitId: string;
}