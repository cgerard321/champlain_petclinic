import axiosInstance from '@/shared/api/axiosInstance';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

export const deleteVet = async (vetId: string): Promise<VetResponseModel> => {
  return await axiosInstance.delete(`/vets/${vetId}`);
};
