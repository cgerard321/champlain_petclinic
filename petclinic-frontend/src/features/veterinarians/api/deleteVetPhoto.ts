import axiosInstance from '@/shared/api/axiosInstance';

export const deleteVetPhoto = async (vetId: string): Promise<void> => {
  await axiosInstance.delete(`/vets/${vetId}/photo`, {
    useV2: false,
  });
};
