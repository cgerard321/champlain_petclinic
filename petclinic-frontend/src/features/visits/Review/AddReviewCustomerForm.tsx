import ReviewModal from './reviewComponents/ReviewModal';
import { FormEvent, useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { addReview } from './Api/addReview';
import { ReviewRequestDTO } from './Model/ReviewRequestDTO';
import './AddForm.css';
import { useUser } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import StarRating from '../../products/components/StarRating';
import { CustomerResponseModel } from '../../customers/models/CustomerResponseModel.ts';
import { getCustomer } from '../../customers/api/getCustomer.ts';
import { getReviewEligibility } from './Api/getReviewEligibility';
import { useTranslation } from 'react-i18next';
import axios from 'axios';

import { cleanReviewText, isReviewProfane } from './ReviewProfanity';

interface ApiError {
  message: string;
}

const AddCustomerReviewForm: React.FC = (): JSX.Element => {
  const { t } = useTranslation('visits');
  const [eligibilityStatus, setEligibilityStatus] = useState<
    'loading' | 'eligible' | 'ineligible' | 'error'
  >('loading');
  const [review, setReview] = useState<ReviewRequestDTO>({
    rating: 0,
    ownerId: '',
    reviewerName: '',
    review: '',
    dateSubmitted: new Date(),
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const [showProfanityModal, setShowProfanityModal] = useState(false);
  const [maskedPreview, setMaskedPreview] = useState<string>('');

  const navigate = useNavigate();
  const { user } = useUser(); // Assuming this hook provides the user info

  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  useEffect(() => {
    if (!user?.userId) return;

    let active = true;

    setEligibilityStatus('loading');

    getReviewEligibility()
      .then(({ eligible }) => {
        if (active) {
          setEligibilityStatus(eligible ? 'eligible' : 'ineligible');
        }
      })
      .catch(error => {
        console.error('Failed to check review eligibility:', error);
        if (active) {
          setEligibilityStatus('error');
        }
      });

    return () => {
      active = false;
    };
  }, [user?.userId]);

  useEffect(() => {
    if (user?.userId) {
      getCustomer(user.userId)
        .then(res => {
          const data: CustomerResponseModel = res.data;

          setReview(prev => ({
            ...prev,
            ownerId: data.customerId,
            reviewerId: data.customerId,
            reviewerName: `${data.firstName} ${data.lastName}`,
          }));
        })
        .catch(err => {
          console.error('Failed to fetch owner:', err);
        });
    }
  }, [user]);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    const { name, value } = e.target;
    setReview(prevReview => ({
      ...prevReview,
      [name as keyof ReviewRequestDTO]:
        name === 'rating' ? parseInt(value, 10) : value,
    }));
  };

  const handleCancel = (): void => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/visits');
    }
  };

  const MAX_REVIEW_LEN = 200;

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!review.review?.trim()) newErrors.review = 'Review text is required';
    if (!review.rating || review.rating < 1 || review.rating > 5)
      newErrors.rating = 'Rating must be between 1 and 5';
    if ((review.review || '').length > MAX_REVIEW_LEN)
      newErrors.review = `Max ${MAX_REVIEW_LEN} characters`;
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    // Validate form inputs
    if (!validate()) return;

    if (isReviewProfane(review.review || '')) {
      setMaskedPreview(cleanReviewText(review.review || ''));
      setShowProfanityModal(true);
      return;
    }

    // Set loading state
    setIsLoading(true);
    setErrorMessage('');

    const payload: ReviewRequestDTO = {
      ...review,
      ownerId: review.ownerId || user?.userId || '',
      dateSubmitted: new Date(),
    };

    try {
      // Call the addCustomerReview API with the updated review state
      await addReview(payload);

      // Success handling
      setShowNotification(true);

      // Auto-hide success message after 3 seconds
      setTimeout(() => setShowNotification(false), 3000);

      // Navigate to a different page or reset the form
      navigate(AppRoutePaths.CustomerReviews);
      setReview({
        rating: 0,
        ownerId: '',
        reviewerName: '',
        review: '',
        dateSubmitted: new Date(),
      });
    } catch (error) {
      const apiError = error as ApiError;

      if (axios.isAxiosError(error) && error.response?.status === 403) {
        setEligibilityStatus('ineligible');
        setErrorMessage(t('reviews.completedAppointmentRequired'));
      } else {
        setErrorMessage(
          `Error adding review: ${apiError?.message || 'Unknown error'}`
        );
      }
    } finally {
      setIsLoading(false);
    }
  };

  const closeProfanityModal = (): void => {
    setShowProfanityModal(false);
    requestAnimationFrame(() => textareaRef.current?.focus());
  };

  if (eligibilityStatus !== 'eligible') {
    const message =
      eligibilityStatus === 'ineligible'
        ? t('reviews.completedAppointmentRequired')
        : eligibilityStatus === 'error'
          ? t('reviews.eligibilityUnavailable')
          : t('reviews.checkingEligibility');

    return (
      <div className="add-review-form">
        <h2>Add a Customer Review</h2>

        <p className="text-muted" role="status" aria-live="polite">
          {message}
        </p>

        <button className="cancel" type="button" onClick={handleCancel}>
          Cancel
        </button>
      </div>
    );
  }

  return (
    <div className="add-review-form">
      <h2>Add a Customer Review</h2>
      {isLoading && <div className="loader"></div>}
      <form onSubmit={handleSubmit}>
        <div className="rating-section">
          <label>Rating:</label>
          <StarRating
            currentRating={review.rating}
            viewOnly={false}
            updateRating={rating => setReview(prev => ({ ...prev, rating }))}
          ></StarRating>
          {errors.rating && <span className="error">{errors.rating}</span>}
        </div>
        <div>
          <label>Your Name:</label>
          <p className="reviewer-name-display">
            {review.reviewerName || 'Loading...'}
          </p>
        </div>
        <div>
          <label>Review:</label>
          <textarea
            name="review"
            ref={textareaRef}
            value={review.review}
            onChange={handleInputChange}
            required
            rows={5}
            maxLength={MAX_REVIEW_LEN}
          />
          {errors.review && <span className="error">{errors.review}</span>}
        </div>
        <button className="cancel" type="button" onClick={handleCancel}>
          Cancel
        </button>
        <button type="submit">Submit</button>
      </form>

      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {showNotification && (
        <div className="notification">Review added successfully!</div>
      )}
      <ReviewModal
        open={showProfanityModal}
        onClose={closeProfanityModal}
        maskedPreview={maskedPreview}
      />
    </div>
  );
};

export default AddCustomerReviewForm;
