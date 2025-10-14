import axiosInstance from '@/shared/api/axiosInstance';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

export const deleteVet = async (vetId: string): Promise<VetResponseModel> => {
  const response = await axiosInstance.delete<VetResponseModel>(
    `/vets/${vetId}`,
    {
      useV2: false,
    }
  );
  return response.data;
};
