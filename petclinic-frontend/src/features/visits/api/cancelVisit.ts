import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';

export async function cancelVisit(
  visitId: string,
  onSuccess: (updatedVisit: Visit) => void
): Promise<void> {
  const confirmCancel = window.confirm(
    'Do you confirm you want to cancel the reservation?'
  );

  if (!confirmCancel) return;

  try {
    const requestBody = { status: 'CANCELLED' };
    await axiosInstance.patch(`/visits/${visitId}`, requestBody, {
      useV2: false,
    });

    // Fetch the updated visit data from the backend
    const updatedVisitResponse = await axiosInstance.get<Visit>(
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
