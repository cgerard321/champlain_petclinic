// src/Api/getEmergencyVisits.ts
import { EmergencyResponseDTO } from '../Model/EmergencyResponseDTO';

export const getEmergencyVisits = async (
  userId: string
): Promise<EmergencyResponseDTO[]> => {
  const response = await fetch(
    `http://localhost:8080/api/v2/gateway/visits/emergency/owners/${userId}`,
    {
      headers: { Accept: 'application/json' }, // Adjust if necessary
      credentials: 'include',
    }
  );

  if (!response.ok) {
    throw new Error(
      `Error fetching emergency visits: ${response.status} ${response.statusText}`
    );
  }

  return response.json();
};
