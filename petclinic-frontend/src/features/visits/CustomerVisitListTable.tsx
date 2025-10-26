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
import BasicModal from '@/shared/components/BasicModal';
import '@/shared/components/BasicModal.css';
import { FaCalendarAlt } from 'react-icons/fa';

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
          visitData = await getAllVetVisits(user.userId);
        } else {
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

      if (!(blob instanceof Blob) || blob.size === 0) {
        setErrorDialogMessage('No prescription is associated with this visit.');
        setShowErrorDialog(true);
        return;
      }

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = downloadName || `prescription-${visitId}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      if (axios.isAxiosError(e) && e.response?.status === 404) {
        setErrorDialogMessage('No prescription is associated with this visit.');
        setShowErrorDialog(true);
        return;
      }
      setErrorDialogMessage('An unexpected error occurred.');
      setShowErrorDialog(true);
    }
  };

  useEffect(() => {
    if (showErrorDialog) {
      const trigger = document.getElementById('error-modal-trigger');
      trigger?.click();
    }
  }, [showErrorDialog]);

  return (
    <div>
      <div className="visit-actions">
        <button
          className="btn btn-primary"
          onClick={() => navigate(AppRoutePaths.CustomerVisitsCalendar)}
          title="Calendar View"
        >
          <FaCalendarAlt className="me-2" />
          Calendar View
        </button>
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
                      Download Prescription
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showErrorDialog && (
        <BasicModal
          title="No Prescription Available"
          confirmText="OK"
          onConfirm={async () => {
            setShowErrorDialog(false);
            navigate(AppRoutePaths.CustomerVisits);
          }}
          showButton={
            <button id="error-modal-trigger" style={{ display: 'none' }} />
          }
        >
          <p className="basic-modal-body">
            {errorDialogMessage ??
              'An error occurred while downloading the prescription.'}
          </p>
        </BasicModal>
      )}
    </div>
  );
}
