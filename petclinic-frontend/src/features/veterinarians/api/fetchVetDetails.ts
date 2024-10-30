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
    const response = await fetch(
      `http://localhost:8080/api/v2/gateway/vets/${vetId}`
    );

    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }

    const data: VetResponseType = await response.json();
    return data;
  } catch (error) {
    throw new Error('Failed to fetch vet details');
  }
};
