import { useEffect, useState } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import DeleteVet from '@/pages/Vet/DeleteVet.tsx';

export default function VetListTable(): JSX.Element {
  const [vets, setVets] = useState<VetRequestModel[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchVets = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/vets`,
          {
            headers: {
              Accept: 'application/json',
            },
            credentials: 'include',
          }
        );

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

    fetchVets();
  }, []);


  const handleVetDelete = (vetId: string): void => {
    setVets(prevVets => prevVets.filter(vet => vet.vetId !== vetId));
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
            {vets.map(vet => (
              <tr key={vet.vetId}>
                <td>{vet.firstName}</td>
                <td>{vet.lastName}</td>
                <td>
                  {vet.specialties.map(specialty => specialty.name).join(', ')}
                </td>
                <td>
                  <DeleteVet
                    vetId={vet.vetId}
                    onVetDeleted={() => handleVetDelete(vet.vetId)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
