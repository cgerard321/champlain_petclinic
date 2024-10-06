import axiosInstance from '@/shared/api/axiosInstance.ts';
import { EmailModelResponseDTO } from '@/features/Emailing/Model/EmailResponse.ts';
import { RawEmailModelDTO } from '@/features/Emailing/Model/RawEmailModel.ts';
// eslint-disable-next-line no-unused-vars,@typescript-eslint/no-unused-vars
import axios, { AxiosError } from 'axios';

export async function SendRawEmail(
  payload: RawEmailModelDTO
): Promise<{ status: number; message: string }> {
  try {
    const response = await axiosInstance.post<EmailModelResponseDTO>(
      'http://localhost:8080/api/v2/gateway/emailing/send/raw',
      payload
    );
    return { status: response.status, message: 'Email sent successfully' };
  } catch (error: AxiosError | unknown) {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status || 500; // Get status code or fallback to 500
      const message = error.response?.data?.message || 'Error sending email';
      return { status, message };
    } else {
      return { status: 500, message: 'An unknown error occurred' };
    }
  }
}
