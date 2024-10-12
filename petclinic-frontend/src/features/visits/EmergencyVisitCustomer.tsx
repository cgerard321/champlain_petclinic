import { useEffect, useState } from 'react';
import { useUser } from '@/context/UserContext';
import { EmergencyResponseDTO } from './Emergency/Model/EmergencyResponseDTO';
import './Emergency.css';
import { useNavigate } from 'react-router-dom';

export default function EmergencyVisitCustomer(): JSX.Element {
  const { user } = useUser();
  const [emergencies, setEmergencies] = useState<EmergencyResponseDTO[]>([]);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    if (!user.userId) return;

    const fetchEmergencyVisits = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/visits/emergency/owners/${user.userId}`,
          {
            headers: { Accept: 'text/event-stream' },
            credentials: 'include',
          }
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const reader = response.body?.getReader();
        const decoder = new TextDecoder('utf-8');

        let done = false;
        const emergenciesArray: EmergencyResponseDTO[] = [];

        while (!done) {
          const { value, done: streamDone } = (await reader?.read()) || {};
          done = streamDone || true;

          if (value) {
            const chunk = decoder.decode(value, { stream: true });
            const formattedChunks = chunk.trim().split(/\n\n/);

            formattedChunks.forEach(formattedChunk => {
              const cleanChunk = formattedChunk.trim().replace(/^data:\s*/, '');

              if (cleanChunk) {
                try {
                  const newEmergency: EmergencyResponseDTO =
                    JSON.parse(cleanChunk);
                  emergenciesArray.push(newEmergency);
                  setEmergencies([...emergenciesArray]);
                } catch (e) {
                  setError('Error parsing chunk');
                }
              }
            });
          }
        }
      } catch (err) {
        if (err instanceof Error) {
          setError(`Failed to fetch emergency visits: ${err.message}`);
        } else {
          setError('Failed to fetch emergency visits');
        }
      }
    };

    fetchEmergencyVisits();
  }, [user.userId]);

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
              <th>Pet Birthdate </th>
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
                <td> {new Date(emergency.vetBirthDate).toLocaleString()}</td>
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
