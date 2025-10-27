import * as React from 'react';
import './reviewModal.styles.css';

type ReviewModalProps = {
  open: boolean;
  onClose: () => void;
  maskedPreview: string;
};

const ReviewModal: React.FC<ReviewModalProps> = ({ open, maskedPreview }) => {
  if (!open) return null;

  return (
    <div>
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
      </div>
    </div>
  );
};

export default ReviewModal;
