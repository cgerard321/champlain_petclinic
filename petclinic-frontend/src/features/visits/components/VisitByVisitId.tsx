import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
// Assuming this function gets the visit by visitId
import { Visit } from '../models/Visit';
import './VisitByVisitId.css';
import { getVisit } from '../api/getVisit';
import { cancelVisit } from '../api/cancelVisit';
import CancellationModal from '@/features/visits/components/CancellationModal.tsx';
import RescheduleModal from '@/features/visits/components/RescheduleModal.tsx';
import { confirmVisit } from '../api/confirmVisit';
import { rescheduleVisit } from '../api/rescheduleVisit';
import AutoDismissAlert from '@/shared/components/AutoDismissAlert';
// import EditingVisit from './EditingVisit';
// import { updateVisit } from '../api/updateVisit';

export default function VisitDetails(): JSX.Element {
  const { visitId } = useParams<{ visitId: string }>(); // Extract visitId from URL parameters
  const [visit, setVisit] = useState<Visit | null>(null); // State for the visit
  const navigate = useNavigate();
  const { t } = useTranslation('visits');
  const [alertShow, setAlertShow] = useState(false);
  const [alertMessage, setAlertMessage] = useState('');
  const [alertType, setAlertType] = useState<'success' | 'error'>('success');

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

  const showAlert = (message: string, type: 'success' | 'error'): void => {
    setAlertMessage(message);
    setAlertType(type);
    setAlertShow(true);
  };

  const isFollowUp = !!visit.followUpOfVisitId;
  const isMoreThan24HoursAway =
    new Date(visit.visitDate).getTime() - Date.now() > 24 * 60 * 60 * 1000;
  const isConfirmedFollowUp = isFollowUp && visit.status === 'CONFIRMED';
  const canChangeFollowUp =
    isFollowUp && isMoreThan24HoursAway && visit.status === 'UPCOMING';

  const handleConfirm = async (): Promise<void> => {
    try {
      await confirmVisit(visit.visitId, updatedVisit => setVisit(updatedVisit));
      showAlert(t('followUp.confirmed'), 'success');
    } catch {
      showAlert(t('followUp.error'), 'error');
    }
  };

  const handleReschedule = async (visitDate: string): Promise<void> => {
    try {
      await rescheduleVisit(visit.visitId, visitDate, updatedVisit =>
        setVisit(updatedVisit)
      );
      showAlert(t('followUp.rescheduled'), 'success');
    } catch {
      showAlert(t('followUp.error'), 'error');
    }
  };

  return (
    <div className="visit-details-container">
      <h1 className="visit-details-title">Visit Details</h1>
      {isFollowUp && (
        <div className="follow-up-label-container">
          <span className="follow-up-label">{t('followUp.label')}</span>
        </div>
      )}

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
            <span className="visit-value">{visit.cancellationReason}</span>
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

        {visit.status === 'COMPLETED' && visit.vetNote && (
          <div className="visit-field">
            <span className="visit-label">Vet Note:</span>
            <span className="visit-value">{visit.vetNote}</span>
          </div>
        )}
      </div>

      <div className="button-visit-details">
        <button
          className="btn btn-warning"
          onClick={() => navigate('/customer/visits')}
          title="Let a review"
        >
          Return to visits
        </button>

        {(visit.status === 'CONFIRMED' || visit.status === 'UPCOMING') &&
          !isConfirmedFollowUp && (
            <CancellationModal
              showButton={<button className="btn btn-danger">Cancel</button>}
              onConfirm={cancellationRequest =>
                cancelVisit(visit.visitId, cancellationRequest, updatedVisit =>
                  setVisit(updatedVisit)
                )
              }
            />
          )}

        {canChangeFollowUp && (
          <button className="btn btn-success" onClick={handleConfirm}>
            {t('followUp.confirm')}
          </button>
        )}

        {canChangeFollowUp && (
          <RescheduleModal
            showButton={
              <button className="btn btn-primary">
                {t('followUp.reschedule')}
              </button>
            }
            visit={visit}
            onConfirm={handleReschedule}
          />
        )}
      </div>

      {isConfirmedFollowUp && (
        <p className="follow-up-note">{t('followUp.confirmedNote')}</p>
      )}

      {isFollowUp && !isMoreThan24HoursAway && visit.status === 'UPCOMING' && (
        <p className="follow-up-note">{t('followUp.tooLate')}</p>
      )}

      <AutoDismissAlert
        show={alertShow}
        message={alertMessage}
        type={alertType}
        duration={5000}
        onClose={() => setAlertShow(false)}
      />
    </div>
  );
}
