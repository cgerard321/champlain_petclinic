import axiosInstance from '@/shared/api/axiosInstance';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';

export const updateVisit = async (
  visitId: string,
  visit: VisitRequestModel
): Promise<void> => {
  await axiosInstance.put<void>(`/visits/${visitId}`, visit);
};

export const getVisit = async (
  visitId: string
): Promise<VisitResponseModel> => {
  const response = await axiosInstance.get<VisitResponseModel>(
    `http://localhost:8080/api/v2/gateway/visits/${visitId}`
  );
  return response.data; // Return only the data
};
