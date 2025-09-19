import axiosInstance from '@/shared/api/axiosInstance';
import { Visit } from '../../models/Visit';

export const getVisitByVisitId = async (visitId: string): Promise<Visit> => {
  const response = await axiosInstance.get<Visit>(
    `/visits/${visitId}`
  );
  return response.data; // Return only the data
};
