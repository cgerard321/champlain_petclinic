import axios from 'axios';

export const fetchVetPhoto = async (vetId: string): Promise<string> => {
  try {
    const response = await axios.get(
      `http://localhost:8080/api/v2/gateway/vets/${vetId}/photo`,
      {
        responseType: 'blob',
        headers: {
          Accept: 'image/*',
        },
      }
    );

    const blob = response.data;
    return URL.createObjectURL(blob);
  } catch (error) {
    console.error('Failed to fetch vet photo:', error);
    return '/images/vet_default.jpg';
  }
};
