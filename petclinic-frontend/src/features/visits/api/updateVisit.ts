import axiosInstance from '@/shared/api/axiosInstance';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel.ts';

//TODO Make this use v1
export const updateVisit = async (
  visitId: string,
  visit: VisitRequestModel
): Promise<void> => {
  await axiosInstance.put<void>(`/visits/${visitId}`, visit, { useV2: false });
};
