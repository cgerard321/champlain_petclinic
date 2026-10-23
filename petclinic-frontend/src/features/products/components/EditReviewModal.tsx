import { useTranslation } from 'react-i18next';
import { useState, useEffect, useMemo } from 'react';
import { Modal, Button } from 'react-bootstrap';
import StarRating from './StarRating.tsx';
import ReviewBox from './ReviewBox.tsx';
import {
  RatingCategories,
  RatingModel,
} from '../models/ProductModels/RatingModel';
import './EditReviewModal.css';

interface EditReviewModalProps {
  show: boolean;
  onClose: () => void;
  currentUserRating: RatingModel;
  updateRating: (
    rating: number,
    review: string | null,
    categoryRatings: RatingCategories
  ) => Promise<void>;
}

const EditReviewModal = ({
  show,
  onClose,
  currentUserRating,
  updateRating,
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
}: EditReviewModalProps) => {
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
      setError('updateRatingRequired');
      return;
    }
    if (!reviewText.trim()) {
      setError('updateReviewRequired');
      return;
    }
    if (reviewText.length > 10000) {
      setError('reviewTooLong');
      return;
    }
    if (Object.values(categoryRatings).some(rating => rating === 0)) {
      setError('categoryRatingsRequired');
      return;
    }

    try {
      await updateRating(localRating, reviewText, categoryRatings);
      setError('');
      onClose();
    } catch (err) {
      const responseError = err as {
        response?: {
          data?: {
            message?: string;
          };
        };
      };

      if (
        responseError.response?.data?.message ===
        'This review is no longer editable'
      ) {
        setError('reviewEditPeriodEnded');
      } else {
        setError('updateFailed');
      }

      console.error('Failed to update review:', err);
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
      dialogClassName="erm-modal-dialog"
    >
      <div className="erm-close-container">
        <button onClick={onClose} className="erm-close-btn">
          ×
        </button>
      </div>

      <Modal.Body className="erm-body">
        <p className="erm-title">{t('editReview')}</p>
        <p className="erm-subtitle">{t('updateHint')}</p>

        {error && <div className="erm-error">{t(error)}</div>}

        {/* Rating Section */}
        <div className="erm-section">
          <label className="erm-label">{t('yourRating')}</label>
          <StarRating
            currentRating={localRating}
            viewOnly={false}
            updateRating={newRating => setLocalRating(newRating)}
          />
        </div>

        {/* Rating Categories Section */}
        <div className="erm-section">
          <label className="erm-label">{t('qualityOfProduct')} *</label>
          <StarRating
            currentRating={categoryRatings.qualityRating}
            viewOnly={false}
            updateRating={rating =>
              updateCategoryRating('qualityRating', rating)
            }
          />
        </div>

        <div className="erm-section">
          <label className="erm-label">{t('valueOfProduct')} *</label>
          <StarRating
            currentRating={categoryRatings.valueRating}
            viewOnly={false}
            updateRating={rating => updateCategoryRating('valueRating', rating)}
          />
        </div>

        <div className="erm-section">
          <label className="erm-label">{t('petSatisfaction')} *</label>
          <StarRating
            currentRating={categoryRatings.petSatisfactionRating}
            viewOnly={false}
            updateRating={rating =>
              updateCategoryRating('petSatisfactionRating', rating)
            }
          />
        </div>

        <div className="erm-section">
          <label className="erm-label">{t('ingredients')} *</label>
          <StarRating
            currentRating={categoryRatings.ingredientsRating}
            viewOnly={false}
            updateRating={rating =>
              updateCategoryRating('ingredientsRating', rating)
            }
          />
        </div>

        {/* Review Section */}
        <div className="erm-section">
          <label className="erm-label">{t('yourReview')}</label>
          <ReviewBox updateFunc={setReviewText} rating={memoizedRating} />
        </div>

        {/* Buttons */}
        <div className="erm-button-container">
          <Button className="cancel-btn" onClick={onClose}>
            {t('cancel')}
          </Button>
          <Button onClick={handleSubmit}>{t('updateReview')}</Button>
        </div>
      </Modal.Body>
    </Modal>
  );
};

export default EditReviewModal;
