import axiosInstance from '@/shared/api/axiosInstance';
import { ImageModel } from '../models/ImageModel';
export async function getImage(imageId: string): Promise<ImageModel> {
  try {
    const response = await axiosInstance.get(`http://localhost:8080/api/v2/gateway/images/${imageId}`, {
      responseType: 'json',
    });
    return response.data;
  } catch (error) {
    throw new Error('Error fetching image');
  }
}