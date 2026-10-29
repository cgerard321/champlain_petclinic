import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

export interface TimeSlot {
  startTime: Date;
  endTime: Date;
  available: boolean;
}
export default interface EmergencyAvailability {
  veterinarian: VetResponseModel;
  availableSlots: TimeSlot[];
}
