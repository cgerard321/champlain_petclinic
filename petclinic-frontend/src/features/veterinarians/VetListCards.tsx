import { useState } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import { useNavigate } from 'react-router-dom';
import './VetListCard.css';
import DeleteVet from '@/pages/Vet/DeleteVet.tsx';
import { deleteVet } from '@/features/veterinarians/api/deleteVet';
import UpdateVet from '@/pages/Vet/UpdateVet';

interface VetCardTableProps {
  vets: VetRequestModel[];
  onDeleteVet: (updatedVets: VetRequestModel[]) => void;
}

export default function VetCardTable({
  vets,
  onDeleteVet,
}: VetCardTableProps): JSX.Element {
  const navigate = useNavigate();
  const [selectedVet, setSelectedVet] = useState<VetRequestModel | null>(null);

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
            className="card"
            onClick={() => handleCardClick(vet.vetId)}
          >
            <div className="card-content">
              <h3>
                {vet.firstName} {vet.lastName}
              </h3>
              <p>
                Specialties:{' '}
                {vet.specialties.map(specialty => specialty.name).join(', ')}
              </p>
              <div className="card-actions">
                <button
                  className="btn btn-primary"
                  onClick={event => {
                    event.stopPropagation();
                    setSelectedVet(vet);
                  }}
                >
                  Update
                </button>
                <DeleteVet
                  vetId={vet.vetId}
                  onVetDeleted={event => handleVetDelete(event, vet.vetId)}
                />
              </div>
            </div>
          </div>
        ))
      )}
      {selectedVet && (
        <UpdateVet vet={selectedVet} onClose={() => setSelectedVet(null)} />
      )}
    </div>
  );
}
