import ReviewModal from './ReviewModal';
import { FormEvent, useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { addReview } from '../Api/addReview';
import { ReviewRequestDTO } from '../Model/ReviewRequestDTO';
import '../AddForm.css';
import { useUser } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import StarRating from '../../../products/components/StarRating';
import { OwnerResponseModel } from '../../../customers/models/OwnerResponseModel';
import { getOwner } from '../../../customers/api/getOwner';

import { Filter } from 'bad-words';
import BasicModal from '@/shared/components/BasicModal';

const filter = new Filter();
filter.addWords('badWord', 'anotherBadWord'); // Custom bad words for demo purposes

interface ApiError {
  message: string;
}

const AddingReview: React.FC = (): JSX.Element => {
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
    if (user?.userId) {
      getOwner(user.userId)
        .then(res => {
          const data: OwnerResponseModel = res.data;

          setReview(prev => ({
            ...prev,
            ownerId: data.ownerId,
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

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!review.review?.trim()) newErrors.review = 'Review text is required';
    if (!review.rating || review.rating < 1 || review.rating > 5)
      newErrors.rating = 'Rating must be between 1 and 5';

    if (filter.isProfane(review.review || '')) {
      setMaskedPreview(filter.clean(review.review || ''));
      setShowProfanityModal(true);
      newErrors.profanity = 'No profanities!';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    // Validate form inputs
    if (!validate()) return;

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

      setTimeout(() => {
        window.location.reload();
      }, 1000);
    } catch (error) {
      // Error handling
      const apiError = error as ApiError;
      setErrorMessage(
        `Error adding review: ${apiError?.message || 'Unknown error'}`
      );
    } finally {
      // Reset loading state
      setIsLoading(false);
    }
  };

  const closeProfanityModal = (): void => {
    setShowProfanityModal(false);
    requestAnimationFrame(() => textareaRef.current?.focus());
  };

  return (
    <BasicModal
      formId="review-form"
      title="Add a Customer Review"
      validate={validate}
      showButton={
        <button className="btn btn-primary" title="Add Review">
          Add Review
        </button>
      }
      errorMessage={errorMessage}
    >
      {isLoading && <div className="loader"></div>}
      <form id="review-form" onSubmit={handleSubmit}>
        <div className="rating-section">
          <div>
            <label>Your Name:</label>
            <p className="reviewer-name-display">
              {review.reviewerName || 'Loading...'}
            </p>
          </div>
          <label>
            Rating:{' '}
            {errors.rating && <span className="error">{errors.rating}</span>}
          </label>
          <StarRating
            currentRating={review.rating}
            viewOnly={false}
            updateRating={rating => setReview(prev => ({ ...prev, rating }))}
          ></StarRating>
        </div>

        <div>
          <label>
            Review:{' '}
            {errors.review && <span className="error">{errors.review}</span>}
          </label>
          <textarea
            name="review"
            ref={textareaRef}
            value={review.review}
            onChange={handleInputChange}
            required
            rows={5}
          />
        </div>
      </form>

      {showNotification && (
        <div className="notification">Review added successfully!</div>
      )}
      <ReviewModal
        open={showProfanityModal}
        onClose={closeProfanityModal}
        maskedPreview={maskedPreview}
      />
    </BasicModal>
  );
};

export default AddingReview;
