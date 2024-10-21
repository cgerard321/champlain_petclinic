import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { VisitResponseModel } from '../models/VisitResponseModel';

export const addVisitForOwner = async (
  ownerId: string,
  visit: VisitRequestModel
): Promise<VisitResponseModel> => {
  try {
    const response: AxiosResponse<VisitResponseModel> =
      await axiosInstance.post(`visits/owners/${ownerId}`, visit);

    // Handle both 200 and 201 status codes as success
    if (response.status !== 200 && response.status !== 201) {
      throw new Error(`Error: ${response.status} ${response.statusText}`);
    }

    return response.data;
  } catch (error) {
    console.error('Failed to add visit:', error);
    throw error;
  }
};
