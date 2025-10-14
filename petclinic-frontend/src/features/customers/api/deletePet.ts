import axiosInstance from '@/shared/api/axiosInstance';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export const deletePet = async (petId: string) => {
  return await axiosInstance.delete(`/pets/${petId}`, { useV2: false });
};
