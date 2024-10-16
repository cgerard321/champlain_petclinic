import axiosInstance from '@/shared/api/axiosInstance';
import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';

export const getAllEmergency = async (): Promise<EmergencyResponseDTO[]> => {
  const response = await axiosInstance.get<EmergencyResponseDTO[]>(
    `http://localhost:8080/api/v2/gateway/visits/emergency`
  );
  //console.log('API response:', response); // Log the full response
  return response.data; // Return only the data
};

export const getAllEmergencyForOwner = async (
  userId: string
): Promise<EmergencyResponseDTO[]> => {
  const response = await axiosInstance.get<EmergencyResponseDTO[]>(
    `http://localhost:8080/api/v2/gateway/visits/emergency/owners/${userId}`
  );
  return response.data; // Return only the data
};
