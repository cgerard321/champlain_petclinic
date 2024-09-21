import axiosInstance from '@/shared/api/axiosInstance';
import { Visit } from '../../models/Visit';

export const getVisitByVisitId = async (visitId: string): Promise<Visit> => {
  const response = await axiosInstance.get<Visit>(
    `http://localhost:8080/api/gateway/visits/${visitId}`
  );
  return response.data; // Return only the data
};
