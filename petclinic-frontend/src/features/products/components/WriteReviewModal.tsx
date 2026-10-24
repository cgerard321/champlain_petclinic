import { useTranslation } from 'react-i18next';
import { useEffect, useMemo, useState } from 'react';
import { Button, Modal } from 'react-bootstrap';
import StarRating from './StarRating';
import ReviewBox from './ReviewBox';
import {
  RatingCategories,
  RatingModel,
} from '../models/ProductModels/RatingModel';

import './WriteReviewModal.css';

interface WriteReviewModalProps {
  show: boolean;
  onClose: () => void;
  currentUserRating: RatingModel;
  updateRating: (
    rating: number,
    review: string | null,
    categoryRatings: RatingCategories
  ) => Promise<void>;
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
  const [categoryRatings, setCategoryRatings] = useState<RatingCategories>({
    qualityRating: currentUserRating.qualityRating ?? 0,
    valueRating: currentUserRating.valueRating ?? 0,
    petSatisfactionRating: currentUserRating.petSatisfactionRating ?? 0,
    ingredientsRating: currentUserRating.ingredientsRating ?? 0,
  });
  const [error, setError] = useState<string>('');

  useEffect(() => {
    setReviewText(currentUserRating.review);
    setLocalRating(currentUserRating.rating);
    setError('');
    setCategoryRatings({
      qualityRating: currentUserRating.qualityRating ?? 0,
      valueRating: currentUserRating.valueRating ?? 0,
      petSatisfactionRating: currentUserRating.petSatisfactionRating ?? 0,
      ingredientsRating: currentUserRating.ingredientsRating ?? 0,
    });
  }, [currentUserRating, show]);

  const updateCategoryRating = (
    category: keyof RatingCategories,
    rating: number
  ): void => {
    setCategoryRatings(current => ({
      ...current,
      [category]: rating,
    }));
  };

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
    if (reviewText.length > 10000) {
      setError('reviewTooLong');
      return;
    }

    try {
      await updateRating(localRating, reviewText, categoryRatings);
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
      fullscreen
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

        {/* Rating Categories Section */}
        <div className="wrm-section">
          <label className="wrm-label">{t('qualityOfProduct')}</label>
          <StarRating
            currentRating={categoryRatings.qualityRating}
            viewOnly={false}
            updateRating={rating =>
              updateCategoryRating('qualityRating', rating)
            }
          />
        </div>

        <div className="wrm-section">
          <label className="wrm-label">{t('valueOfProduct')}</label>
          <StarRating
            currentRating={categoryRatings.valueRating}
            viewOnly={false}
            updateRating={rating => updateCategoryRating('valueRating', rating)}
          />
        </div>

        <div className="wrm-section">
          <label className="wrm-label">{t('petSatisfaction')}</label>
          <StarRating
            currentRating={categoryRatings.petSatisfactionRating}
            viewOnly={false}
            updateRating={rating =>
              updateCategoryRating('petSatisfactionRating', rating)
            }
          />
        </div>

        <div className="wrm-section">
          <label className="wrm-label">{t('ingredients')}</label>
          <StarRating
            currentRating={categoryRatings.ingredientsRating}
            viewOnly={false}
            updateRating={rating =>
              updateCategoryRating('ingredientsRating', rating)
            }
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
