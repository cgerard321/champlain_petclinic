import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ReceivedEmailModel } from '@/features/Emailing/Model/ReceivedEmailModel.ts';

export async function GetAllReceivedEmails(): Promise<ReceivedEmailModel[]> {
  const response = await axiosInstance.get<ReceivedEmailModel[]>(
    'http://localhost:8080/api/v2/gateway/emailing/received/all',
    {
      responseType: 'json', // Set the response type as JSON
    }
  );

  return response.data.map((email: ReceivedEmailModel) => ({
    from: email.from,
    subject: email.subject,
    dateReceived: new Date(email.dateReceived), // Ensure dateReceived is a Date object
    plainTextBody: email.plainTextBody,
  }));
}
