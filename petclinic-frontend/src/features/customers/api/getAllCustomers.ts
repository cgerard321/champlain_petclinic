import axiosInstance from '@/shared/api/axiosInstance.ts';
import { CustomerResponseModel } from '../models/CustomerResponseModel.ts';

export async function getAllCustomers(): Promise<CustomerResponseModel[]> {
  const response = await axiosInstance.get('/customers', {
    responseType: 'text',
    useV2: false,
  });
  return response.data
    .split('data:')
    .map((payLoad: string) => {
      try {
        if (payLoad == '') return null;
        return JSON.parse(payLoad);
      } catch (err) {
        console.error("Can't parse JSON: " + err);
      }
    })
    .filter((data?: JSON) => data !== null);
}
