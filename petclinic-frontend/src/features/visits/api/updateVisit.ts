import axiosInstance from '@/shared/api/axiosInstance';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel.ts';

export const updateVisit = async (
  visitId: string,
  visit: Partial<VisitRequestModel>
): Promise<void> => {
  await axiosInstance.patch<void>(`/visits/${visitId}`, visit, {
    useV2: false,
  });
};
