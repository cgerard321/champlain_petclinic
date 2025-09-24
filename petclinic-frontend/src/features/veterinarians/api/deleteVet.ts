import axiosInstance from '@/shared/api/axiosInstance';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

export const deleteVet = async (vetId: string): Promise<VetResponseModel> => {
  const response = await axiosInstance.delete<VetResponseModel>(`/vets/${vetId}`);
  return response.data;
};
