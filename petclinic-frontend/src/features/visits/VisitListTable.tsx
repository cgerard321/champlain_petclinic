import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import { getAllVisits } from './api/getAllVisits';

export default function VisitListTable(): JSX.Element {
  const [visitsList, setVisitsList] = useState<Visit[]>([]);

  useEffect(() => {
    const fetchVisits = async (): Promise<void> => {
      try {
        const visits = await getAllVisits();
        setVisitsList(visits);
      } catch (error) {
        console.error('Error fetching visits:', error);
      }
    };

    fetchVisits();
  }, []);

  return (
    <table>
      <thead>
        <tr>
          <th>Visit Id</th>
          <th>Visit Date</th>
          <th>Description</th>
          <th>Pet Name</th>
          <th>Vet First Name</th>
          <th>Vet Last Name</th>
          <th>Vet Email</th>
          <th>Vet Phone</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {visitsList.map(visit => (
          <tr key={visit.visitId}>
            <td>{visit.visitId}</td>
            <td>{visit.visitDate}</td>
            <td>{visit.description}</td>
            <td>{visit.petName}</td>
            <td>{visit.vetFirstName}</td>
            <td>{visit.vetLastName}</td>
            <td>{visit.vetEmail}</td>
            <td>{visit.vetPhone}</td>
            <td>{visit.status}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
