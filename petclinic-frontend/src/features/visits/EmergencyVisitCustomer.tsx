/* eslint-disable react-hooks/exhaustive-deps */
import { useEffect, useState } from 'react';
import { useUser } from '@/context/UserContext';
import { EmergencyResponseDTO } from './Emergency/Model/EmergencyResponseDTO';
import './Emergency.css';
import { useNavigate } from 'react-router-dom';
import { getAllEmergencyForOwner } from './Emergency/Api/getAllEmergency';

export default function EmergencyVisitCustomer(): JSX.Element {
  const { user } = useUser();
  const [emergencies, setEmergencies] = useState<EmergencyResponseDTO[]>([]);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  type EmergencyData = string | EmergencyResponseDTO[];
  useEffect(() => {
    const fetchEmergencyVisits = async (): Promise<void> => {
      if (!user.userId) return; // Ensure userId is available

      try {
        const response = await getAllEmergencyForOwner(user.userId);
        const formattedResponse = formatEmergencyData(response);
        if (Array.isArray(formattedResponse)) {
          setEmergencies(formattedResponse); // Set the emergencies if response is an array
        } else {
          console.error('Fetched data is not an array:', formattedResponse);
        }
      } catch (error) {
        console.error('Error fetching emergency visits:', error);
        setError('Failed to fetch emergency visits');
      }
    };

    fetchEmergencyVisits().catch(error =>
      console.error('Error in fetchEmergencyVisits:', error)
    );
  }, [user.userId]);

  const formatEmergencyData = (data: EmergencyData): EmergencyResponseDTO[] => {
    if (typeof data === 'string') {
      // If the data is a string that starts with 'data:', split and parse it
      const formattedData = data
        .split('\n\n')
        .map(chunk => {
          const cleanChunk = chunk.trim().replace(/^data:\s*/, '');
          if (cleanChunk) {
            return JSON.parse(cleanChunk); // Parse each valid chunk
          }
          return null;
        })
        .filter((item): item is EmergencyResponseDTO => item !== null); // Filter to ensure only EmergencyResponseDTO types

      return formattedData; // Return as EmergencyResponseDTO[]
    }

    return []; // Return an empty array if data is not in the expected format
  };

  return (
    <div>
      <button
        className="btn btn-dark"
        onClick={() => navigate('/add/emergency')}
        title="Create emergency visit"
      >
        Create Emergency visit
      </button>
      {error ? (
        <p>{error}</p>
      ) : (
        <table className="visit-table-section-red">
          <thead>
            <tr>
              <th>Visit Date</th>
              <th>Description</th>
              <th> PetId</th>
              <th>Pet Name</th>
              <th> PractitionnerId</th>
              <th>vetFirstName</th>
              <th>vetLastName</th>
              <th>Email</th>
              <th>Phone Number</th>
            </tr>
          </thead>
          <tbody>
            {emergencies.map(emergency => (
              <tr key={emergency.visitEmergencyId}>
                <td>{new Date(emergency.visitDate).toLocaleString()}</td>
                <td>{emergency.description}</td>
                <td> {emergency.petId}</td>
                <td>{emergency.petName}</td>
                <td>{emergency.practitionerId}</td>
                <td>{emergency.vetFirstName}</td>
                <td>{emergency.vetLastName}</td>
                <td>{emergency.vetEmail}</td>
                <td>{emergency.vetPhoneNumber}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
