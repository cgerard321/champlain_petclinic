import * as React from 'react';
import './reviewModal.styles.css';

type ReviewModalProps = {
  open: boolean;
  onClose: () => void;
  maskedPreview: string;
};

const ReviewModal: React.FC<ReviewModalProps> = ({
  open,
  onClose,
  maskedPreview,
}) => {
  if (!open) return null;

  const onBackdropClick: React.MouseEventHandler<HTMLDivElement> = e => {
    if (e.currentTarget === e.target) onClose();
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="review-modal-title"
      className="modal-overlay"
      onClick={onBackdropClick}
    >
      <div className="modal-card" onClick={e => e.stopPropagation()}>
        <h3 id="review-modal-title" style={{ marginTop: 0 }}>
          Woah! Watch your language, pal!
        </h3>

        <div className="modal-body">
          <p>
            It seems like your review contains some words that are not
            appropriate. Please change it before submitting.
          </p>
          <div className="profanity-preview">
            <strong>Preview:</strong>
            <br />
            {maskedPreview}
          </div>
        </div>

        <div className="modal-actions">
          <button onClick={onClose} className="btn-primary">
            Go back
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReviewModal;
