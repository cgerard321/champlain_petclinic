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
      className="review-modal__overlay"
      onClick={onBackdropClick}
    >
      <div className="review-modal__card" onClick={e => e.stopPropagation()}>
        <h3 id="review-modal-title" className="review-modal__title">
          Woah! Watch your language, pal!
        </h3>

        <div className="review-modal__body">
          <p>
            It seems like your review contains some words that are not
            appropriate. Please change it before submitting.
          </p>
          <div className="review-modal__preview">
            <strong>Preview:</strong>
            <br />
            {maskedPreview}
          </div>
        </div>

        <div className="review-modal__actions">
          <button onClick={onClose} className="review-modal__btn-primary">
            Go back
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReviewModal;
