import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerResponseModel } from '../models/CustomerResponseModel.ts';
import { FileDetails } from '@/shared/models/FileDetails';

export const uploadCustomerPhoto = async (
  customerId: string,
  file: File
): Promise<AxiosResponse<CustomerResponseModel>> => {
  const fileData = await convertFileToBase64(file);

  const photoRequest: FileDetails = {
    fileName: file.name,
    fileType: file.type,
    fileData: fileData,
  };

  const response = await axiosInstance.patch<CustomerResponseModel>(
    `/customers/${customerId}/photo`,
    photoRequest,
    {
      useV2: false,
    }
  );

  if (!response.data) {
    throw new Error('Photo upload failed: the server did not save the photo');
  }

  return response;
};

const convertFileToBase64 = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      const base64Data = result.split(',')[1];
      resolve(base64Data);
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
};
