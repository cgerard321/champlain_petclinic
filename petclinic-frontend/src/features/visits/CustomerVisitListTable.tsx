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

  // helper to normalize API responses to an array of visits
  const normalizeVisits = (payload: any): Visit[] => {
    if (!payload) return [];
    if (Array.isArray(payload)) return payload;
    if (payload.visits && Array.isArray(payload.visits)) return payload.visits;
    if (payload.data && Array.isArray(payload.data)) return payload.data;
    if (typeof payload === 'object') return [payload] as Visit[];
    return [];
  };

  // Sort visits: emergency visits first, then by start date
  const sortVisits = (visitsList: Visit[]): Visit[] => {
    return [...visitsList].sort((a, b) => {
      // Emergency visits come first
      if (a.isEmergency && !b.isEmergency) return -1;
      if (!a.isEmergency && b.isEmergency) return 1;

      // Within the same emergency status, sort by start date (most recent first)
      return new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime();
    });
  };
  // Filter visits based on status
  // Ensure we operate on a safe array even if displayedVisits was set incorrectly
  const safeDisplayedVisits: Visit[] = Array.isArray(displayedVisits)
    ? displayedVisits
    : normalizeVisits(displayedVisits);

  const emergencyVisits = sortVisits(
    safeDisplayedVisits.filter(visit => visit.isEmergency)
  );
  const confirmedVisits = sortVisits(
    safeDisplayedVisits.filter(visit => {
      return visit.status === 'CONFIRMED';
    })
  );
  const upcomingVisits = sortVisits(
    safeDisplayedVisits.filter(visit => {
      return visit.status === 'UPCOMING';
    })
  );
  const completedVisits = sortVisits(
    safeDisplayedVisits.filter(visit => {
      return visit.status === 'COMPLETED';
    })
  );
  const cancelledVisits = sortVisits(
    safeDisplayedVisits.filter(visit => {
      return visit.status === 'CANCELLED';
    })
  );
  const archivedVisits = sortVisits(
    safeDisplayedVisits.filter(visit => {
      return visit.status === 'ARCHIVED';
    })
  );

  const categories: Category[] = [
    { name: 'All', list: safeDisplayedVisits },
    { name: 'Emergencies', emergency: true, list: emergencyVisits },
    { name: 'Confirmed', list: confirmedVisits },
    { name: 'Upcoming', list: upcomingVisits },
    { name: 'Completed', list: completedVisits },
    { name: 'Cancelled', list: cancelledVisits },
    { name: 'Archived', list: archivedVisits },
  ];

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

        const list = normalizeVisits(visitData);
        setVisits(list);
        setDisplayedVisits(list);
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
