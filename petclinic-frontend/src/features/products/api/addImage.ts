import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ImageModel } from '../models/ProductModels/ImageModel';

export async function addImage(formData: FormData): Promise<ImageModel> {
  try {
    const response = await axiosInstance.post('/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      responseType: 'json',
    });
    return response.data as ImageModel;
  } catch (error) {
    throw new Error('Error uploading image');
  }
}
