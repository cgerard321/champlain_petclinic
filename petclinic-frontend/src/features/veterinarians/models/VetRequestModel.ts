import { Workday } from './Workday.ts';
import { Speciality } from './Speciality.ts';

export interface VetRequestModel {
  vetId: string;
  vetBillId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: Workday[];
  workHoursJson: string;
  active: boolean;
  specialties: Speciality[];
  photoDefault: boolean;
}
