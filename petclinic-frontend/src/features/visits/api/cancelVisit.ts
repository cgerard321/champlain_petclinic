import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';

export async function cancelVisit(
  visitId: string,
  onSuccess: (updatedVisit: Visit) => void
): Promise<void> {
  try {
    // Fetch existing visit so we can include required fields (like visitDate)
    const existingResponse = await axiosInstance.get<VisitResponseModel>(
      `/visits/${visitId}`,
      {
        useV2: false,
      }
    );
    const existing = existingResponse.data;

    const requestBody = {
      visitDate: existing.visitDate,
      description: existing.description,
      petId: existing.petId,
      practitionerId: existing.practitionerId,
      isEmergency: existing.isEmergency,
      status: 'CANCELLED',
    };

    await axiosInstance.put(`/visits/${visitId}`, requestBody, {
      useV2: false,
    });

    // Fetch the updated visit data from the backend
    const updatedVisitResponse = await axiosInstance.get<VisitResponseModel>(
      `/visits/${visitId}`,
      {
        useV2: false,
      }
    );
    const updatedVisit = updatedVisitResponse.data;

    // Call the success callback with the updated visit
    onSuccess(updatedVisit);

    alert('Visit cancelled successfully!');
  } catch (error) {
    console.error('Error canceling visit:', error);
    alert('Error canceling visit.');
    throw error;
  }
}
