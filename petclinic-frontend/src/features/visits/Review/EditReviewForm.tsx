import * as React from 'react';
import { FormEvent, useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ReviewRequestDTO } from './Model/ReviewRequestDTO';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import './EditForm.css';
import { getReview } from './Api/getReview';
import { updateReview } from './Api/editReview';

// Define an interface for the error if known
interface ApiError {
  message: string;
}

const EditReviewForm: React.FC = (): JSX.Element => {
  const { reviewId } = useParams<{ reviewId: string }>(); // Get reviewId from URL params
  const [review, setReview] = useState<ReviewRequestDTO>({
    rating: 0,
    ownerId: '',
    reviewerName: '',
    review: '',
    dateSubmitted: new Date(),
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchReviewData = async (): Promise<void> => {
      if (reviewId) {
        try {
          const response: ReviewResponseDTO = await getReview(reviewId);
          setReview({
            rating: response.rating,
            ownerId: response.ownerId,
            reviewerName: response.reviewerName,
            review: response.review,
            dateSubmitted: new Date(response.dateSubmitted),
          });
        } catch (error) {
          console.error(`Error fetching review with ID ${reviewId}:`, error);
        }
      }
    };

    fetchReviewData().catch(error =>
      console.error('Error in fetchReviewData:', error)
    );
  }, [reviewId]);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    const { name, value } = e.target;
    setReview(prevReview => ({
      ...prevReview,
      [name as keyof ReviewRequestDTO]:
        name === 'rating' ? parseInt(value) : value,
    }));
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setReview(prevReview => ({
      ...prevReview,
      dateSubmitted: new Date(e.target.value),
    }));
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (review.rating < 1 || review.rating > 5)
      newErrors.rating = 'Rating must be between 1 and 5';
    if (!review.reviewerName)
      newErrors.reviewerName = 'Reviewer name is required';
    if (!review.review) newErrors.review = 'Review text is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      if (reviewId) {
        await updateReview(reviewId, review);
        setSuccessMessage('Review updated successfully!');
        setShowNotification(true);
        setTimeout(() => setShowNotification(false), 3000); // Hide notification after 3 seconds
        navigate('/reviews'); // Navigate to a different page or clear form
      }
    } catch (error) {
      // Use type assertion or check error type
      const apiError = error as ApiError;
      setErrorMessage(`Error updating review: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="edit-review-form">
      <h2>Edit Review</h2>
      {isLoading && <div className="loader">Loading...</div>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Rating:</label>
          <input
            type="number"
            name="rating"
            value={isNaN(review.rating) ? '' : review.rating}
            onChange={handleInputChange}
            min="1"
            max="5"
            required
          />
          {errors.rating && <span className="error">{errors.rating}</span>}
        </div>
        <div>
          <label>Your Name:</label>
          <input
            type="text"
            name="reviewerName"
            value={review.reviewerName}
            onChange={handleInputChange}
            required
          />
          {errors.reviewerName && (
            <span className="error">{errors.reviewerName}</span>
          )}
        </div>
        <div>
          <label>Review:</label>
          <textarea
            name="review"
            value={review.review}
            onChange={handleInputChange}
            required
          />
          {errors.review && <span className="error">{errors.review}</span>}
        </div>
        <div>
          <label>Date Submitted:</label>
          <input
            type="date"
            name="dateSubmitted"
            value={review.dateSubmitted.toISOString().split('T')[0]}
            onChange={handleDateChange}
            required
          />
        </div>
        <button type="submit">Update Review</button>
      </form>

      {successMessage && <p className="success-message">{successMessage}</p>}
      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {showNotification && (
        <div className="notification">Review updated successfully!</div>
      )}
    </div>
  );
};

export default EditReviewForm;
