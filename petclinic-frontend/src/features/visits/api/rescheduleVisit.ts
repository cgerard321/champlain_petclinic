import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';

export async function rescheduleVisit(
  visitId: string,
  visitDate: string,
  onSuccess: (updatedVisit: Visit) => void
): Promise<void> {
  try {
    const patchResponse = await axiosInstance.patch<VisitResponseModel>(
      `/visits/${visitId}/reschedule`,
      { visitDate },
      { useV2: false }
    );

    onSuccess(patchResponse.data);
  } catch (error) {
    console.error('Error rescheduling visit:', error);
    throw error;
  }
}
