import { useEffect, useState } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import { deleteVet } from '@/features/veterinarians/api/deleteVet.ts';
import { Button } from 'react-bootstrap';

export default function VetListTable(): JSX.Element {
  const [vets, setVets] = useState<VetRequestModel[]>([]);
  const [error, setError] = useState<string | null>(null);

  const fetchVets = async (): Promise<void> => {
    try {
      const response = await fetch(`http://localhost:8080/api/v2/gateway/vets`, {
        headers: {
          Accept: 'application/json',
        },
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      setVets(data);
    } catch (err) {
      console.error('Error fetching vets:', err);
      setError('Failed to fetch vets');
    }
  };

  useEffect(() => {
    fetchVets();
  }, []);

  const handleDelete = async (vetId: string) => {
    if (window.confirm('Are you sure you want to delete this vet?')) {
      try {
        await deleteVet(vetId);
        alert('Vet deleted successfully');
        fetchVets();
      } catch (error) {
        console.error('Error deleting vet:', error);
        alert('Failed to delete vet');
      }
    }
  };

  return (
      <div>
        {error ? (
            <p>{error}</p>
        ) : (
            <table className="table table-striped">
              <thead>
              <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Specialties</th>
                <th></th>
              </tr>
              </thead>
              <tbody>
              {vets.map((vet) => (
                  <tr key={vet.vetId}>
                    <td>{vet.firstName}</td>
                    <td>{vet.lastName}</td>
                    <td>
                      {vet.specialties.map((specialty) => specialty.name).join(', ')}
                    </td>
                    <td>
                      <Button
                          variant="danger"
                          onClick={() => handleDelete(vet.vetId)}
                      >
                        Delete
                      </Button>
                    </td>
                  </tr>
              ))}
              </tbody>
            </table>
        )}
      </div>
  );
}