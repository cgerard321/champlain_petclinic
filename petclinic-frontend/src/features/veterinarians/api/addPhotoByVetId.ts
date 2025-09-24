import { AxiosError } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

interface ErrorResponse {
  message?: string;
  error?: string;
  statusCode?: string;
  timestamp?: string;
}

export const addPhotoByVetId = async (
  vetId: string,
  photoName: string,
  file: File
): Promise<void> => {
  if (
    !vetId ||
    !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
      vetId
    )
  ) {
    throw new Error(
      'Invalid Vet ID format. Please ensure you selected a valid vet.'
    );
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    await axiosInstance.post(`/vets/${vetId}/photos/${photoName}`, formData, {
      useV2: false,
    });
    return;
  } catch (err) {
    const axiosError = err as AxiosError;
    const errorData = axiosError.response?.data as ErrorResponse;

    if (axiosError.response?.status === 401) {
      throw new Error('Your session has expired. Please log in again.');
    }
    if (axiosError.response?.status === 403) {
      throw new Error('You do not have permission to upload photos.');
    }
    if (axiosError.response?.status === 404) {
      throw new Error(`Vet with ID ${vetId} not found.`);
    }
    if (axiosError.response?.status === 413) {
      throw new Error('File is too large. Please select a smaller image.');
    }
    if (axiosError.response?.status === 415) {
      throw new Error('Unsupported file type. Use JPG/PNG/etc.');
    }
    if (axiosError.response?.status === 405) {
      throw new Error(
        'Method not allowed (405). Are you hitting the gateway (8080)?'
      );
    }

    throw new Error(
      errorData?.message ||
        errorData?.error ||
        'Failed to upload photo. Please try again.'
    );
  }
};

export default addPhotoByVetId;
