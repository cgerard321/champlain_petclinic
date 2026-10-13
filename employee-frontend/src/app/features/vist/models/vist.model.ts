export interface VisitResponseModel {
  visitId: string;
  visitDate: string;
  visitEndDate: string;
  description: string;
  petId: string;
  petName: string;
  petBirthDate: string;
  practitionerId: string;
  vetFirstName: string;
  vetLastName: string;
  vetEmail: string;
  vetPhoneNumber: string;
  status: string;
  isEmergency: boolean;
  ownerFirstName: string;
  ownerLastName: string;
  ownerEmail?: string;
  ownerPhoneNumber?: string;
}
