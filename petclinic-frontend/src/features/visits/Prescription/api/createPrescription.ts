import axios from 'axios';
import { PrescriptionRequestDTO } from '../models/PrescriptionRequestDTO';
import { PrescriptionResponseDTO } from '../models/PrescriptionResponseDTO';

export const createPrescription = async (
  visitId: string,
  prescription: PrescriptionRequestDTO
): Promise<PrescriptionResponseDTO> => {
  const response = await axios.post(
    `/visits/${visitId}/prescriptions`,
    prescription,
    { useV2: false }
  );
  return response.data;
};
