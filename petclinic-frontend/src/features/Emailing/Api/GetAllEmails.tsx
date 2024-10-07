import axiosInstance from '@/shared/api/axiosInstance.ts';
import { EmailModelResponseDTO } from '@/features/Emailing/Model/EmailResponse.ts';

export async function getAllEmails(): Promise<EmailModelResponseDTO[]> {
  const response = await axiosInstance.get<EmailModelResponseDTO[]>(
    'http://localhost:8080/api/v2/gateway/emailing'
  );

  // Check if the response status is 204 (No Content)
  if (response.status === 204) {
    return [];
  }

  return response.data;
}
