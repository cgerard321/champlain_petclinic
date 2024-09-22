import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
// Assuming this function gets the visit by visitId
import { Visit } from '../models/Visit';
import { getVisitByVisitId } from '../Review/Api/getVisitByVisitId';
import './VisitDetails.css';

export default function VisitDetails(): JSX.Element {
  const { visitId } = useParams<{ visitId: string }>(); // Extract visitId from URL parameters
  const [visit, setVisit] = useState<Visit | null>(null); // State for the visit

  useEffect(() => {
    if (visitId) {
      getVisitByVisitId(visitId)
        .then(response => {
          setVisit(response);
        })
        .catch(error => {
          console.error('Error fetching visit:', error);
        });
    }
  });
  [visitId];

  if (!visit) {
    return <div>Loading...</div>;
  }

  return (
    <div className="visit-details-container">
      <h1 className="visit-details-title">Visit Details</h1>
      <div className="visit-info">
        <div className="visit-field">
          <span className="visit-label">Visit ID:</span>
          <span className="visit-value">{visit.visitId}</span>
        </div>
        <div className="visit-field">
          <span className="visit-label">Visit Date:</span>
          <span className="visit-value">{visit.visitDate}</span>
        </div>
        <div className="visit-field">
          <span className="visit-label">Description:</span>
          <span className="visit-value">{visit.description}</span>
        </div>
        <div className="visit-field">
          <span className="visit-label">Pet Name:</span>
          <span className="visit-value">{visit.petName}</span>
        </div>
        <div className="visit-field">
          <span className="visit-label">Vet First Name:</span>
          <span className="visit-value">{visit.vetFirstName}</span>
        </div>
        <div className="visit-field">
          <span className="visit-label">Vet Last Name:</span>
          <span className="visit-value">{visit.vetLastName}</span>
        </div>
        <div className="visit-field">
          <span className="visit-label">Vet Email:</span>
          <span className="visit-value">{visit.vetEmail}</span>
        </div>
        <div className="visit-field">
          <span className="visit-label">Status:</span>
          <span
            className="visit-value"
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
          </span>
        </div>
      </div>
    </div>
  );
}
