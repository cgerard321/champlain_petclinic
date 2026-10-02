import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
// Assuming this function gets the visit by visitId
import { Visit } from '../models/Visit';
import './VisitByVisitId.css';
import { getVisit } from '../api/getVisit';
import { cancelVisit } from '../api/cancelVisit';
import CancellationModal from "@/features/visits/components/CancellationModal.tsx";
// import EditingVisit from './EditingVisit';
// import { updateVisit } from '../api/updateVisit';

export default function VisitDetails(): JSX.Element {
  const { visitId } = useParams<{ visitId: string }>(); // Extract visitId from URL parameters
  const [visit, setVisit] = useState<Visit | null>(null); // State for the visit
  const navigate = useNavigate();

  useEffect(() => {
    if (visitId) {
      getVisit(visitId)
          .then(response => {
            setVisit(response);
          })
          .catch(error => {
            console.error('Error fetching visit:', error);
          });
    }
  }, [visitId]);

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
                          : visit.status === 'CANCELLED'
                              ? 'red'
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

          {visit.status === 'CANCELLED' && visit.cancellationReason && (
              <div className="visit-field">
                <span className="visit-label">Cancellation Reason:</span>
                <span className="visit-value">
            {visit.cancellationReason}
          </span>
              </div>
          )}


          {visit.status === 'CANCELLED' &&
              visit.cancellationReason === 'OTHER' &&
              visit.cancellationReasonDetails && (
                  <div className="visit-field">
                    <span className="visit-label">Cancellation Details:</span>
                    <span className="visit-value">
              {visit.cancellationReasonDetails}
            </span>
                  </div>
              )}

          <div className="visit-field">
            <span className="visit-label">Visit End Date:</span>
            <span className="visit-value">{visit.visitEndDate}</span>
          </div>
        </div>

        <div className="button-visit-details">
          <button
              className="btn btn-warning"
              onClick={() => navigate('/customer/visits')}
              title="Let a review"
          >
            Return to visits
          </button>

          {(visit.status === 'CONFIRMED' || visit.status === 'UPCOMING') && (
              <CancellationModal
                  showButton={
                    <button className="btn-cancel">
                      Cancel
                    </button>
                  }
                  onConfirm={cancellationRequest =>
                      cancelVisit(
                          visit.visitId,
                          cancellationRequest,
                          updatedVisit => setVisit(updatedVisit)
                      )
                  }
              />
          )}
        </div>
      </div>
  );
}
