import { PrescriptionRequestDTO } from './PrescriptionRequestDTO';

export interface PrescriptionResponseDTO extends PrescriptionRequestDTO {
  prescriptionId: string;
  date: string;
}
