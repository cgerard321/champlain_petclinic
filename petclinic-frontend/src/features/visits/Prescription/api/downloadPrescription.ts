import axios from 'axios';

export const downloadPrescription = async (
  visitId: string,
  prescriptionId: string
): Promise<Blob> => {
  const response = await axios.get(
    `/visits/${visitId}/prescriptions/${prescriptionId}/pdf`,
    { responseType: 'blob' }
  );
  return response.data;
};
