import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';

export async function archiveVisit(
  visitId: string,
  onSuccess: (updatedVisit: Visit) => void
): Promise<void> {
  try {
    const putResponse = await axiosInstance.patch<VisitResponseModel>(
      `/visits/${visitId}/status/ARCHIVED`,
      {},
      { useV2: false }
    );
    const updatedVisit = putResponse.data;

    onSuccess(updatedVisit);
  } catch (error) {
    console.error('Error archiving visit:', error);
    throw error;
  }
}
