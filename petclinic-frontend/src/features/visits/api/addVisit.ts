import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel.ts';

export const addVisit = async (
  visit: VisitRequestModel
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>('/visits', visit, { useV2: false });
};
