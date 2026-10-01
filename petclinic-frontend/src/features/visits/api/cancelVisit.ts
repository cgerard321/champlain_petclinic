import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
import {CancellationRequest} from "@/features/visits/models/CancellationRequest.ts";


export async function cancelVisit(
  visitId: string,
  cancellation:CancellationRequest,
  onSuccess: (updatedVisit: Visit) => void
): Promise<void> {
  try {
    const patchResponse = await axiosInstance.patch<VisitResponseModel>(
      `/visits/${visitId}/status/cancel`,
      cancellation,
      { useV2: false }
    );

    const updatedVisit = patchResponse.data

    onSuccess(updatedVisit);
  } catch (error) {
    console.error('Error canceling visit:', error);
    throw error;
  }
}
