import { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import StarRating from './StarRating.tsx';
import ReviewBox from './ReviewBox.tsx';
import { RatingModel } from '../models/ProductModels/RatingModel';

import './WriteReviewModal.css';

interface WriteReviewModalProps {
  show: boolean;
  onClose: () => void;
  currentUserRating: RatingModel;
  updateRating: (rating: number, review: string | null) => void;
}

const WriteReviewModal = ({
  show,
  onClose,
  currentUserRating,
  updateRating,
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
}: WriteReviewModalProps) => {
  const [reviewText, setReviewText] = useState<string>(
    currentUserRating.review
  );
  const [localRating, setLocalRating] = useState<number>(
    currentUserRating.rating
  );
  const [error, setError] = useState<string>('');

  // Keep modal in sync when parent changes rating or review
  useEffect(() => {
    setReviewText(currentUserRating.review);
    setLocalRating(currentUserRating.rating);
    setError('');
  }, [currentUserRating, show]);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleSubmit = async () => {
    if (localRating === 0) {
      setError('Please select a rating before submitting.');
      return;
    }
    if (!reviewText.trim()) {
      setError('Please write a review before submitting.');
      return;
    }

    try {
      await updateRating(localRating, reviewText);
      setError('');
      onClose();
    } catch (err) {
      setError('Failed to submit review. Please try again.');
      console.error('Failed to submit review:', err);
    }
  };

  return (
    <Modal
      show={show}
      onHide={onClose}
      centered
      dialogClassName="wrm-modal-dialog"
    >
      <div className="wrm-close-container">
        <button onClick={onClose} className="wrm-close-btn">
          ×
        </button>
      </div>

      <Modal.Body className="wrm-body">
        <p className="wrm-title">Write a Review</p>
        <p className="wrm-subtitle">
          Share your experience to help other users make better decisions.
        </p>

        {error && <div className="wrm-error">{error}</div>}

        {/* Rating Section */}
        <div className="wrm-section">
          <label className="wrm-label">Your Rating *</label>
          <StarRating
            currentRating={localRating}
            viewOnly={false}
            updateRating={newRating => setLocalRating(newRating)} // local control
          />
        </div>

        {/* Review Section */}
        <div className="wrm-section">
          <label className="wrm-label">Your Review *</label>
          <ReviewBox
            updateFunc={setReviewText} // local text update
            rating={{ ...currentUserRating, review: reviewText }}
          />
        </div>

        {/* Buttons */}
        <div className="wrm-button-container">
          <Button className="cancel-btn" onClick={onClose}>
            Cancel
          </Button>
          <Button onClick={handleSubmit}>Submit Review</Button>
        </div>
      </Modal.Body>
    </Modal>
  );
};

export default WriteReviewModal;
