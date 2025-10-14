import { useState } from 'react';
import { setInterestExempt } from '../api/setInterestExempt';
import './InterestExemptToggle.css';

interface InterestExemptToggleProps {
  billId: string;
  isExempt: boolean;
  onToggleComplete: () => void;
  variant?: 'simple' | 'detailed';
  disabled?: boolean;
}

export default function InterestExemptToggle({
  billId,
  isExempt,
  onToggleComplete,
  variant = 'simple',
  disabled = false,
}: InterestExemptToggleProps): JSX.Element {
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleToggleClick = (): void => {
    if (disabled) return;
    setShowConfirmation(true);
  };

  const handleConfirm = async (): Promise<void> => {
    setIsLoading(true);
    try {
      await setInterestExempt(billId, !isExempt);
      setShowConfirmation(false);
      onToggleComplete();
    } catch (error) {
      console.error('Failed to update interest exemption:', error);
      alert('Failed to update interest exemption. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = (): void => {
    setShowConfirmation(false);
  };

  if (variant === 'simple') {
    return (
      <>
        <div className="interest-exempt-toggle simple">
          <label className="toggle-switch">
            <input
              type="checkbox"
              checked={isExempt}
              onChange={handleToggleClick}
              disabled={disabled || isLoading}
              className="toggle-checkbox"
            />
            <span className="toggle-slider"></span>
          </label>
          <span
            className={`toggle-label ${isExempt ? 'exempt' : 'not-exempt'}`}
          >
            {isExempt ? 'Exempt' : 'Not Exempt'}
          </span>
        </div>

        {showConfirmation && (
          <div className="confirmation-overlay simple">
            <div className="confirmation-dialog simple">
              <h4>Confirm Change</h4>
              <p>
                {isExempt
                  ? 'Remove interest exemption and allow interest calculations?'
                  : 'Exempt this bill from interest charges?'}
              </p>
              <div className="confirmation-buttons">
                <button
                  className="confirm-button"
                  onClick={handleConfirm}
                  disabled={isLoading}
                >
                  {isLoading ? 'Updating...' : 'Confirm'}
                </button>
                <button
                  className="cancel-button"
                  onClick={handleCancel}
                  disabled={isLoading}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}
      </>
    );
  }

  // Detailed variant (original design)
  return (
    <div className="interest-exempt-toggle detailed">
      <div className="toggle-container">
        <span
          className={`status-indicator ${isExempt ? 'exempt' : 'not-exempt'}`}
        >
          {isExempt ? '✓ Exempt' : '✗ Not Exempt'}
        </span>
        <button
          className={`toggle-button ${isExempt ? 'exempt-btn' : 'not-exempt-btn'}`}
          onClick={handleToggleClick}
          disabled={disabled || isLoading}
          title={
            isExempt ? 'Remove interest exemption' : 'Exempt from interest'
          }
        >
          {isExempt ? 'Remove Exemption' : 'Exempt Interest'}
        </button>
      </div>

      {showConfirmation && (
        <div className="confirmation-overlay detailed">
          <div className="confirmation-dialog detailed">
            <h3>Confirm Interest Exemption Change</h3>
            <p>
              Are you sure you want to{' '}
              <strong>
                {isExempt
                  ? 'remove interest exemption'
                  : 'exempt this bill from interest'}
              </strong>{' '}
              for Bill ID: <code>{billId}</code>?
            </p>
            {!isExempt && (
              <div className="warning-message">
                <strong>⚠️ Note:</strong> Exempting this bill will set the
                interest to $0.00 and prevent future interest calculations.
              </div>
            )}

            {isExempt && (
              <div className="info-message">
                <strong>ℹ️ Note:</strong> Removing exemption will allow interest
                to be calculated based on the bill&apos;s status and due date.
              </div>
            )}

            <div className="confirmation-buttons">
              <button
                className="confirm-button"
                onClick={handleConfirm}
                disabled={isLoading}
              >
                {isLoading ? 'Updating...' : 'Confirm'}
              </button>
              <button
                className="cancel-button"
                onClick={handleCancel}
                disabled={isLoading}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
