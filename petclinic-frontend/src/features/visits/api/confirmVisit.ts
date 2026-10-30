import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';

export async function confirmVisit(
  visitId: string,
  onSuccess: (updatedVisit: Visit) => void
): Promise<void> {
  try {
    const patchResponse = await axiosInstance.patch<VisitResponseModel>(
      `/visits/${visitId}/confirm`,
      undefined,
      { useV2: false }
    );

    onSuccess(patchResponse.data);
  } catch (error) {
    console.error('Error confirming visit:', error);
    throw error;
  }
}
