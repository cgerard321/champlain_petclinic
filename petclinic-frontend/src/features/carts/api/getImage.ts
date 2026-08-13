import axiosInstance from '@/shared/api/axiosInstance';
import { ImageModel } from '../models/ImageModel';

export async function getImage(imageId: string): Promise<ImageModel> {
  try {
    // was http://localhost:8080/api/v2/gateway/images/${imageId}
    const { data } = await axiosInstance.get<ImageModel>(`/images/${imageId}`, {
      responseType: 'json',
      useV2: false,
    });
    return data;
  } catch (error) {
    throw new Error('Error fetching image');
  }
}
