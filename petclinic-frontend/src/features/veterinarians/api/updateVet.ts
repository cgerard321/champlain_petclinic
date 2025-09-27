import axiosInstance from '@/shared/api/axiosInstance';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

export const updateVet = async (
  vetId: string,
  vet: VetRequestModel
): Promise<VetRequestModel> => {
  const response = await axiosInstance.put<VetRequestModel>(
    `/vets/${vetId}`,
    vet,
    {
      useV2: false,
    }
  );
  return response.data;
};

export const getVet = async (userId: string): Promise<VetResponseModel> => {
  const response = await axiosInstance.get<VetResponseModel>(`/vets/${userId}`);
  return response.data;
};
