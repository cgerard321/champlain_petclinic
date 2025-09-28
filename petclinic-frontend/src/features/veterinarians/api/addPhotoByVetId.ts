import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export const addPhotoByVetId = async (
  vetId: string,
  photoName: string,
  file: File
): Promise<AxiosResponse<void>> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('photoName', photoName);

  return axiosInstance.post(`/vets/${vetId}/photos`, formData, {
    useV2: false,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};
