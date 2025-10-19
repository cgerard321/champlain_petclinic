import { useEffect, useState } from 'react';
import { useUser, IsVet } from '@/context/UserContext';
import { Visit } from '@/features/visits/models/Visit.ts';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { getAllOwnerVisits } from './api/getAllOwnerVisits';
import { getAllVetVisits } from './api/getAllVetVisits';
import axios from 'axios';
import { downloadPrescription } from '@/features/visits/Prescription/api/downloadPrescription';
import './CustomerVisitListTable.css';

export default function CustomerVisitListTable(): JSX.Element {
  const { user } = useUser();
  const isVet = IsVet();
  const [visits, setVisits] = useState<Visit[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showErrorDialog, setShowErrorDialog] = useState(false);
  const [errorDialogMessage, setErrorDialogMessage] = useState<string | null>(
    null
  );

  const navigate = useNavigate();

  useEffect(() => {
    if (!user.userId) return;

    const fetchVisits = async (): Promise<void> => {
      try {
        let visitData;
        if (isVet) {
          // Fetch vet's visits
          visitData = await getAllVetVisits(user.userId);
        } else {
          // Fetch owner's visits
          visitData = await getAllOwnerVisits(user.userId);
        }
        if (Array.isArray(visitData)) {
          setVisits(visitData);
        } else {
          console.error('Fetched data is not an array', visitData);
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
  }, [user.userId, isVet]);

  const handleDownloadPrescription = async (
    visitId: string,
    downloadName?: string
  ): Promise<void> => {
    try {
      const blob = await downloadPrescription(visitId);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = downloadName || `prescription-${visitId}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      if (axios.isAxiosError(e)) {
        setErrorDialogMessage('No prescription is associated with this visit.');
        setShowErrorDialog(true);
        return;
      }
      console.error(e);
      setError('Failed to download prescription');
    }
  };

  return (
    <div>
      <div className="visit-actions">
        <button
          className="btn btn-warning"
          onClick={() => navigate(AppRoutePaths.CustomerAddReview)}
          title="Leave a Review"
        >
          Leave a Review
        </button>
        <button
          className="btn btn-dark"
          onClick={() => navigate(AppRoutePaths.CustomerReviews)}
          title="View Reviews"
        >
          View Reviews
        </button>
        <button
          className="btn btn-warning"
          onClick={() => navigate(AppRoutePaths.OwnerBookAppointment)}
          title="Schedule a Visit"
        >
          Schedule a Visit
        </button>
      </div>

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
              <th>Actions</th>
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
                <td
                  style={{
                    color:
                      visit.status === 'CONFIRMED'
                        ? 'green'
                        : visit.status === 'UPCOMING'
                          ? 'orange'
                          : visit.status === 'CANCELLED'
                            ? 'red'
                            : visit.status === 'COMPLETED'
                              ? 'blue'
                              : visit.status === 'ARCHIVED'
                                ? 'gray'
                                : 'inherit',
                    fontWeight: 'bold',
                  }}
                >
                  {visit.status}
                </td>
                <td>
                  {['CONFIRMED', 'UPCOMING', 'COMPLETED'].includes(
                    visit.status
                  ) && (
                    <button
                      type="button"
                      className="btn btn-primary btn-sm"
                      onClick={async ev => {
                        ev.preventDefault();
                        ev.stopPropagation();

                        try {
                          await handleDownloadPrescription(
                            visit.visitId,
                            visit.prescriptionFile?.fileName ||
                              `prescription-${visit.visitId}.pdf`
                          );
                        } catch {}
                      }}
                    >
                      📄 Download Prescription
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showErrorDialog && (
        <div
          className="cvlt-modal-overlay"
          onClick={() => {
            setShowErrorDialog(false);
            navigate(AppRoutePaths.CustomerVisits);
          }}
        >
          <div
            className="cvlt-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cvlt-modal-title"
            onClick={e => e.stopPropagation()}
          >
            <h3 id="cvlt-modal-title" className="cvlt-modal-title">
              Download error
            </h3>
            <p className="cvlt-modal-body">
              {errorDialogMessage ??
                'An error occurred while downloading the prescription.'}
            </p>
            <div className="cvlt-modal-actions">
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => {
                  setShowErrorDialog(false);
                  navigate(AppRoutePaths.CustomerVisits);
                }}
              >
                OK
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
