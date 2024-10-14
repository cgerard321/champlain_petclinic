import { useEffect, useState } from 'react';
import { useUser } from '@/context/UserContext';
import { Visit } from '@/features/visits/models/Visit.ts';

export default function CustomerVisitListTable(): JSX.Element {
  const { user } = useUser();
  const [visits, setVisits] = useState<Visit[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user.userId) return;

    const fetchVisits = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/visits/owners/${user.userId}`,
          {
            headers: {
              Accept: 'text/event-stream',
            },
            credentials: 'include',
          }
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const reader = response.body?.getReader();
        const decoder = new TextDecoder('utf-8');

        let done = false;
        const visitsArray: Visit[] = [];

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
                  const newVisit: Visit = JSON.parse(cleanChunk);
                  visitsArray.push(newVisit);
                  setVisits([...visitsArray]);
                } catch (e) {
                  setError('Error parsing chunk');
                }
              }
            });
          }
        }
      } catch (err) {
        if (err instanceof Error) {
          setError(`Failed to fetch visits: ${err.message}`);
        } else {
          setError('Failed to fetch visits');
        }
      }
    };

    fetchVisits();
  }, [user.userId]);

  return (
    <div>
      {error ? (
        <p>{error}</p>
      ) : (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Visit ID</th>
              <th>Pet Name</th>
              <th>Visit Date</th>
              <th>Visit Description</th>
              <th>Vet Name</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {visits.map(visit => (
              <tr key={visit.visitId}>
                <td>{visit.visitId}</td>
                <td>{visit.petName}</td>
                <td>{visit.visitDate}</td>
                <td>{visit.description}</td>
                <td>{`${visit.vetFirstName} ${visit.vetLastName}`}</td>
                <td>{visit.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
