import * as React from 'react';
import { FormEvent, useState } from 'react';

import BasicModal from '@/shared/components/BasicModal';
import { CancellationReason } from '../models/CancellationReason';
import { CancellationRequest } from '../models/CancellationRequest';

import './CancellationModal.css';

interface CancellationModalProps {
  showButton: JSX.Element;
  onConfirm: (request: CancellationRequest) => Promise<void>;
}

const CancellationModal: React.FC<CancellationModalProps> = ({
  showButton,
  onConfirm,
}) => {
  const [reason, setReason] = useState<CancellationReason | ''>('');
  const [otherReason, setOtherReason] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const validate = (): boolean => {
    if (!reason) {
      setErrorMessage('Please select a cancellation reason.');
      return false;
    }

    if (reason === CancellationReason.OTHER && !otherReason.trim()) {
      setErrorMessage('Please provide a cancellation reason.');
      return false;
    }

    setErrorMessage('');
    return true;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (!validate()) return;

    await onConfirm({
      cancellationReason: reason as CancellationReason,
      cancellationReasonDetails:
        reason === CancellationReason.OTHER ? otherReason.trim() : undefined,
    });
  };

  return (
    <BasicModal
      title="Cancel Visit"
      showButton={showButton}
      formId="cancellationForm"
      validate={validate}
      confirmText="Confirm Cancellation"
      errorMessage={errorMessage}
    >
      <form id="cancellationForm" onSubmit={handleSubmit}>
        <p>Select a reason for cancelling this visit.</p>

        <label className="cancellation-option">
          <input
            type="radio"
            name="cancellationReason"
            value={CancellationReason.APPOINTMENT_NO_LONGER_NEEDED}
            onChange={e => {
              setReason(e.target.value as CancellationReason);
              setErrorMessage('');
            }}
          />
          Appointment is no longer needed
        </label>

        <label className="cancellation-option">
          <input
            type="radio"
            name="cancellationReason"
            value={CancellationReason.PET_OWNER_NO_LONGER_AVAILABLE}
            onChange={e => {
              setReason(e.target.value as CancellationReason);
              setErrorMessage('');
            }}
          />
          Pet owner is no longer available
        </label>

        <label className="cancellation-option">
          <input
            type="radio"
            name="cancellationReason"
            value={CancellationReason.PET_OWNER_NEEDS_TO_RESCHEDULE}
            onChange={e => {
              setReason(e.target.value as CancellationReason);
              setErrorMessage('');
            }}
          />
          Pet owner needs to reschedule
        </label>

        <label className="cancellation-option">
          <input
            type="radio"
            name="cancellationReason"
            value={CancellationReason.OTHER}
            onChange={e => {
              setReason(e.target.value as CancellationReason);
              setErrorMessage('');
            }}
          />
          Other
        </label>

        {reason === CancellationReason.OTHER && (
          <div className="cancellation-other">
            <label htmlFor="otherReason">Reason:</label>

            <textarea
              id="otherReason"
              value={otherReason}
              onChange={e => setOtherReason(e.target.value)}
            />
          </div>
        )}
      </form>
    </BasicModal>
  );
};

export default CancellationModal;
