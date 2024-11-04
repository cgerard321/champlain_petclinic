import axios from 'axios';

interface VetResponseType {
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
export const fetchVetDetails = async (
  vetId: string
): Promise<VetResponseType> => {
  try {
    const response = await axios.get<VetResponseType>(
      `http://localhost:8080/api/v2/gateway/vets/${vetId}`
    );
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(`Error: ${error.response?.statusText || error.message}`);
    } else {
      throw new Error('Failed to fetch vet details');
    }
  }
};
