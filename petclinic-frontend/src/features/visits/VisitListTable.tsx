import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import './VisitListTable.css';
import { useNavigate } from 'react-router-dom';
import {AppRoutePaths} from "@/shared/models/path.routes.ts";

export default function VisitListTable(): JSX.Element {
  const [visitsList, setVisitsList] = useState<Visit[]>([]);
  const navigate = useNavigate();

  // Real-time updates (SEE)
  useEffect(() => {
    const eventSource = new EventSource(
      'http://localhost:8080/api/v2/gateway/visits',
      {
        withCredentials: true,
      }
    );

    eventSource.onmessage = event => {
      try {
        const newVisit: Visit = JSON.parse(event.data);
        setVisitsList(oldVisits => {
          // Check to see if the new visit is already in the list
          if (!oldVisits.some(visit => visit.visitId === newVisit.visitId)) {
            return [...oldVisits, newVisit];
          }
          return oldVisits;
        });
      } catch (error) {
        console.error('Error parsing SSE data:', error);
      }
    };

    // Handle errors
    eventSource.onerror = error => {
      console.error('EventSource error:', error);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, []);

  return (
      <div>
        <button
            className="btn btn-warning"
            onClick={() => navigate('/forms')}
            title="Let a review"
        >
          Leave a Review
        </button>
        <p></p>
        <button
            className="btn btn-dark"
            onClick={() => navigate('/reviews')}
            title="View review"
        >
          View Reviews
        </button>

        <button
            className="btn btn-warning"
            onClick={() => navigate(AppRoutePaths.AddVisit)}
            title="Make a visit"
        >
          Make a visit
        </button>
        <h1>Visits List</h1>
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
                <td
                    style={{
                      color:
                          visit.status === 'CONFIRMED'
                              ? 'green'
                              : visit.status === 'UPCOMING'
                                  ? 'orange'
                                  : visit.status === 'COMPLETED'
                                      ? 'blue'
                                      : 'inherit',
                    }}
                >
                  {visit.status}
                </td>
              </tr>
          ))}
          </tbody>
        </table>
      </div>
  );
}
