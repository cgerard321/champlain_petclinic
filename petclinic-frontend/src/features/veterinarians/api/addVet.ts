import { AxiosResponse } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { Workday } from '@/features/veterinarians/models/Workday.ts';

export const addVet = async (
  vet: {
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
      email: string
    };
    userId: string;
    email: string;
    username: string
  },
): Promise<AxiosResponse<void>> => {
  return await axiosInstance.post<void>('/vets/users/vets', vet);
};
