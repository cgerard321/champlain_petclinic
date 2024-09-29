import { useState } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import { useNavigate } from 'react-router-dom';
import './VetListTable.css';
import DeleteVet from '@/pages/Vet/DeleteVet.tsx';
import { deleteVet } from '@/features/veterinarians/api/deleteVet';
import UpdateVet from '@/pages/Vet/UpdateVet';

interface VetListTableProps {
  vets: VetRequestModel[];
  onDeleteVet: (updatedVets: VetRequestModel[]) => void;
}

export default function VetListTable({
  vets,
  onDeleteVet,
}: VetListTableProps): JSX.Element {
  const navigate = useNavigate();
  const [selectedVet, setSelectedVet] = useState<VetRequestModel | null>(null);

  const handleRowClick = (vetId: string): void => {
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
    <div>
      {vets.length === 0 ? (
        <p>No vets found.</p>
      ) : (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Specialties</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {vets.map(vet => (
              <tr
                key={vet.vetId}
                onClick={() => handleRowClick(vet.vetId)}
                className="clickable-row"
              >
                <td>{vet.firstName}</td>
                <td>{vet.lastName}</td>
                <td>
                  {vet.specialties.map(specialty => specialty.name).join(', ')}
                </td>
                <td>
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
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {selectedVet && (
        <UpdateVet vet={selectedVet} onClose={() => setSelectedVet(null)} />
      )}
    </div>
  );
}
