import axiosInstance from '@/shared/api/axiosInstance';

export interface VetResponseType {
  vetId: string;
  vetBillId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: string[];
  workHoursJson: string;
  active: boolean;
  specialties: { specialtyId: string; name: string }[];
}

export const fetchVet = async (vetId: string): Promise<VetResponseType> => {
  try {
    const response = await axiosInstance.get<VetResponseType>(
      `/vets/${vetId}`,
      {
        useV2: false,
      }
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch vet details:', error);
    throw new Error('Failed to fetch vet details');
  }
};
