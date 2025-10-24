import { useEffect, useState } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import { useNavigate } from 'react-router-dom';
import './VetListCard.css';
import DeleteVet from '@/pages/Vet/DeleteVet.tsx';
import { deleteVet } from '@/features/veterinarians/api/deleteVet';
import { fetchVetPhoto } from './api/fetchPhoto';
import { IsVet, IsAdmin } from '@/context/UserContext';

interface VetCardTableProps {
  vets: VetRequestModel[];
  onDeleteVet: (updatedVets: VetRequestModel[]) => void;
}

export default function VetCardTable({
  vets,
  onDeleteVet,
}: VetCardTableProps): JSX.Element {
  const navigate = useNavigate();
  const isVet = IsVet();
  const isAdmin = IsAdmin();
  const [vetPhotos, setVetPhotos] = useState<{ [key: string]: string }>({});

  useEffect(() => {
    const fetchPhotos = async (): Promise<void> => {
      const photos: { [key: string]: string } = {};
      for (const vet of vets) {
        try {
          const photoUrl = await fetchVetPhoto(vet.vetId);
          photos[vet.vetId] = photoUrl;
        } catch (error) {
          photos[vet.vetId] = '/images/vet_default.jpg';
        }
      }
      setVetPhotos(photos);
    };

    fetchPhotos();
  }, [vets]);

  const handleCardClick = (vetId: string): void => {
    navigate(`/vets/${vetId}`);
  };

  const handleVetDelete = async (
    event: React.MouseEvent,
    vetId: string
  ): Promise<void> => {
    event.stopPropagation();
    try {
      await deleteVet(vetId);
      onDeleteVet(vets.filter(vet => vet.vetId !== vetId));
    } catch (err) {
      console.error('Error deleting vet:', err);
    }
  };

  return (
    <div className="card-container">
      {vets.length === 0 ? (
        <p>No vets found.</p>
      ) : (
        vets.map(vet => (
          <div
            key={vet.vetId}
            className="card-vets"
            onClick={() => handleCardClick(vet.vetId)}
          >
            <div className="photo-container">
              <img
                src={vetPhotos[vet.vetId] || '/images/vet_default.jpg'}
                alt="Vet photo"
                className="card-image"
              />
            </div>

            <div className="card-content">
              <h3>
                {vet.firstName} {vet.lastName}
                {!vet.active && (
                  <span
                    className="inactive-indicator"
                    aria-label="Inactive"
                  ></span>
                )}
              </h3>
              <p>
                <strong>Specialties:</strong>{' '}
                {vet.specialties.map(specialty => specialty.name).join(', ')}
              </p>
              <div className="card-actions">
                {!isVet && isAdmin && (
                  <DeleteVet
                    vetId={vet.vetId}
                    onVetDeleted={event => handleVetDelete(event, vet.vetId)}
                  />
                )}
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
