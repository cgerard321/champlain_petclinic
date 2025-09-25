import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export const getEmergencyVisitsByOwnerId = async (
  ownerId: string
): Promise<EmergencyResponseDTO[]> => {
  return await axiosInstance.get(`/visits/emergency/owners/${ownerId}`, {
    useV2: false,
  });
};
