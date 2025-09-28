import { Bill } from '@/features/bills/models/Bill';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { AxiosResponse, AxiosError } from 'axios';

// Define an interface for the expected error response body
interface ErrorResponse {
  message: string;
}

export async function deleteBill(bill: Bill): Promise<AxiosResponse> {
  try {
    const response: AxiosResponse = await axiosInstance.delete(
      `/bills/${bill.billId}`,
      {
        useV2: false,
      }
    );
    return response;
  } catch (error) {
    const axiosError = error as AxiosError<ErrorResponse>;
    console.error('Error deleting bill:', axiosError);
    if (
      axiosError.response &&
      axiosError.response.data &&
      axiosError.response.data.message
    ) {
      throw new Error(axiosError.response.data.message);
    } else {
      throw new Error('Error deleting bill');
    }
  }
}
