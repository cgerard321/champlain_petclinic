import axiosInstance from '@/shared/api/axiosInstance';
import { ImageModel } from '../models/ProductModels/ImageModel';

export async function getImage(imageId: string): Promise<ImageModel> {
  try {
    const response = await axiosInstance.get('/images/' + imageId, {
      useV2: false,
    });

    return response.data;
  } catch (error) {
    throw new Error('Error fetching image ' + error);
  }
}
