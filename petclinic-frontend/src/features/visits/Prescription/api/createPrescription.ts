import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { PrescriptionRequestDTO } from '../models/PrescriptionRequestDTO';
import { PrescriptionResponseDTO } from '../models/PrescriptionResponseDTO';

export const createPrescription = async (
  visitId: string,
  prescription: PrescriptionRequestDTO
): Promise<AxiosResponse<PrescriptionResponseDTO>> => {
  return await axiosInstance.post<PrescriptionResponseDTO>(
    `/visits/${visitId}/prescriptions`,
    prescription,
    { useV2: false }
  );
};
