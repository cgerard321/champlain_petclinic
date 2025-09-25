import { useEffect, useState } from 'react';
import { getPetPhoto } from '../api/getPetPhoto';

interface PetPhotoContainerProps {
  petId: string;
  petName: string;
}

export default function PetPhotoContainer({
  petId,
  petName,
}: PetPhotoContainerProps): JSX.Element {
  const [photoData, setPhotoData] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<boolean>(false);

  useEffect(() => {
    const fetchPetPhoto = async (): Promise<void> => {
      try {
        setLoading(true);
        setError(false);
        const response = await getPetPhoto(petId);
        const base64Data = response.data.photo;

        if (base64Data) {
          const imageUrl = `data:${response.data.type};base64,${base64Data}`;
          setPhotoData(imageUrl);
        } else {
          setError(true);
        }
      } catch (error) {
        console.error('Failed to fetch pet photo:', error);
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    fetchPetPhoto();
  }, [petId]);

  if (loading) {
    return (
      <div className="pet-photo-container">
        <div className="pet-photo-placeholder">Loading photo...</div>
      </div>
    );
  }

  if (error || !photoData) {
    return (
      <div className="pet-photo-container">
        <div className="pet-photo-placeholder">No photo available</div>
      </div>
    );
  }

  return (
    <div className="pet-photo-container">
      <img src={photoData} alt={`${petName}'s photo`} className="pet-photo" />
    </div>
  );
}
