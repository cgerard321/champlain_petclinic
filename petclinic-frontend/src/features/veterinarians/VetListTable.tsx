import { useEffect, useState } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import { useNavigate } from 'react-router-dom';
import './VetListTable.css';

export default function VetListTable(): JSX.Element {
  const [vets, setVets] = useState<VetRequestModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate(); // Use navigate to programmatically navigate

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

  // Function to handle row click
  const handleRowClick = (vetId: string): void => {
    navigate(`/vets/${vetId}`); // Navigate to the vet details page
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
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
