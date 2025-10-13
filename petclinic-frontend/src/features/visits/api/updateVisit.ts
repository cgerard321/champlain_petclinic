import axiosInstance from '@/shared/api/axiosInstance';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel.ts';
import { VisitResponseModel } from '../models/VisitResponseModel';

export const updateVisit = async (
  visitId: string,
  visit: Partial<VisitRequestModel>
): Promise<void> => {
  await axiosInstance.put<void>(`/visits/${visitId}`, visit, {
    useV2: false,
  });
};

export const updateVisitStatus = async (
  visitId: string,
  status: string
): Promise<VisitResponseModel> => {
  const response = await axiosInstance.put<VisitResponseModel>(
    `/visits/${visitId}/status/${status}`,
    null,
    {
      useV2: false,
    }
  );
  return response.data;
};
