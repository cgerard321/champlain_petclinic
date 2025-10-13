import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Visit } from '@/features/visits/models/Visit.ts';

export async function archiveVisit(
  visitId: string,
  onSuccess: (updatedVisit: Visit) => void
): Promise<void> {
  const confirmArchive = window.confirm(
    `Are you sure you want to archive visit with ID: ${visitId}?`
  );

  if (!confirmArchive) return;

  try {
    const requestBody = { status: 'ARCHIVED' };
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

    alert('Visit archived successfully!');
  } catch (error) {
    console.error('Error archiving visit:', error);
    alert('Error archiving visit.');
    throw error;
  }
}
