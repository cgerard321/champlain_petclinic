import { useTranslation } from 'react-i18next';
import { useState, useEffect, useMemo } from 'react';
import { Modal, Button } from 'react-bootstrap';
import StarRating from './StarRating';
import ReviewBox from './ReviewBox';
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
  const { t } = useTranslation('products');
  const [reviewText, setReviewText] = useState<string>(
    currentUserRating.review
  );
  const [localRating, setLocalRating] = useState<number>(
    currentUserRating.rating
  );
  const [error, setError] = useState<string>('');

  useEffect(() => {
    setReviewText(currentUserRating.review);
    setLocalRating(currentUserRating.rating);
    setError('');
  }, [currentUserRating, show]);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleSubmit = async () => {
    if (localRating === 0) {
      setError('ratingRequired');
      return;
    }
    if (!reviewText.trim()) {
      setError('reviewRequired');
      return;
    }

    try {
      await updateRating(localRating, reviewText);
      setError('');
      onClose();
    } catch (err) {
      setError('submitFailed');
      console.error('Failed to submit review:', err);
    }
  };

  const memoizedRating = useMemo(
    () => ({ ...currentUserRating, review: reviewText }),
    [currentUserRating, reviewText]
  );

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
        <p className="wrm-title">{t('writeReview')}</p>
        <p className="wrm-subtitle">{t('shareExperience')}</p>

        {error && <div className="wrm-error">{t(error)}</div>}

        {/* Rating Section */}
        <div className="wrm-section">
          <label className="wrm-label">{t('yourRating')}</label>
          <StarRating
            currentRating={localRating}
            viewOnly={false}
            updateRating={newRating => setLocalRating(newRating)}
          />
        </div>

        {/* Review Section */}
        <div className="wrm-section">
          <label className="wrm-label">{t('yourReview')}</label>
          <ReviewBox updateFunc={setReviewText} rating={memoizedRating} />
        </div>

        {/* Buttons */}
        <div className="wrm-button-container">
          <Button className="cancel-btn" onClick={onClose}>
            {t('cancel')}
          </Button>
          <Button onClick={handleSubmit}>{t('submitReview')}</Button>
        </div>
      </Modal.Body>
    </Modal>
  );
};

export default WriteReviewModal;
