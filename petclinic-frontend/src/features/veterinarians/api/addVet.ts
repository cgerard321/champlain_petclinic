import axiosInstance from '@/shared/api/axiosInstance';
import { Workday } from '@/features/veterinarians/models/Workday.ts';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel';

export const addVet = async (vet: {
  password: string;
  vet: {
    resume: string;
    firstName: string;
    lastName: string;
    specialties: { specialtyId: string; name: string }[];
    phoneNumber: string;
    workday: Workday[];
    workHoursJson: string;
    photoDefault: boolean;
    vetBillId: string;
    active: boolean;
    vetId: string;
    email: string;
  };
  userId: string;
  email: string;
  username: string;
}): Promise<VetRequestModel> => {
  const response = await axiosInstance.post(`/vets`, vet, {
    useV2: false,
  });
  return response.data;
};
