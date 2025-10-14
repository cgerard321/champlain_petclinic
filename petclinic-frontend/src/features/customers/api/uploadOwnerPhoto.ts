import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerResponseModel } from '../models/OwnerResponseModel';
import { FileRequestDTO } from '../models/FileRequestDTO';

export const uploadOwnerPhoto = async (
  ownerId: string,
  file: File
): Promise<AxiosResponse<OwnerResponseModel>> => {
  const fileData = await convertFileToBase64(file);

  const fileRequest: FileRequestDTO = {
    fileName: file.name,
    fileType: file.type,
    fileData: fileData,
  };

  return await axiosInstance.post<OwnerResponseModel>(
    `/owners/${ownerId}/photo`,
    fileRequest,
    {
      useV2: false,
    }
  );
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
