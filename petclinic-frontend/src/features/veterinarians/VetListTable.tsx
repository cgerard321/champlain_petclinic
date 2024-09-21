import { useEffect, useState } from 'react'; // Import React hooks for managing state and side-effects
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts'; // Import the VetRequestModel for type safety
import { deleteVet } from '@/features/veterinarians/api/deleteVet.ts'; // Import the deleteVet function for the delete operation
import { Button } from 'react-bootstrap'; // Import Bootstrap components

export default function VetListTable(): JSX.Element {
  const [vets, setVets] = useState<VetRequestModel[]>([]); // State to store the list of vets
  const [error, setError] = useState<string | null>(null); // State to handle any errors

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
      setVets(data); // Store the fetched vets in state
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
        await deleteVet(vetId); // Call the delete API
        alert('Vet deleted successfully');
        fetchVets(); // Refresh the list after deletion
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