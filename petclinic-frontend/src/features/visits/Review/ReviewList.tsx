import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllReviews } from './Api/getAllReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import { deleteReview } from './Api/deleteReview';
import { IsOwner, IsAdmin } from '@/context/UserContext';
import BasicModal from '@/shared/components/BasicModal';

const ReviewsList: React.FC = (): JSX.Element => {
  const [reviewList, setReviewList] = useState<ReviewResponseDTO[]>([]);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [showSuccessMessage, setShowSuccessMessage] = useState<boolean>(false);
  const navigate = useNavigate();
  const isOwner = IsOwner();
  const isAdmin = IsAdmin();
  const canAccessActions = isOwner || isAdmin;

  useEffect(() => {
    const fetchReviewsData = async (): Promise<void> => {
      try {
        const response = await getAllReviews();
        if (Array.isArray(response)) {
          setReviewList(response);
        } else {
          console.error('Fetched data is not an array:', response);
        }
      } catch (error) {
        console.error('Error fetching reviews:', error);
      }
    };

    fetchReviewsData().catch(error =>
      console.error('Error in fetchReviewsData:', error)
    );
  }, []);
  const handleDelete = async (reviewId: number): Promise<void> => {
    try {
      await deleteReview(reviewId.toString());

      setReviewList(prev =>
        prev.filter(review => review.reviewId !== reviewId)
      );

      // show a brief success message (mirrors CustomerReviewsList)
      setSuccessMessage('Review successfully deleted.');
      setShowSuccessMessage(true);
      setTimeout(() => {
        setShowSuccessMessage(false);
        setSuccessMessage(null);
      }, 2500);
    } catch (error) {
      console.error('Error deleting review:', error);
    }
  };

  return (
    <div className="reviews-container">
      <h1>Reviews</h1>
      {showSuccessMessage && successMessage && (
        <div
          className="visit-success-message"
          role="status"
          aria-live="polite"
          style={{ marginBottom: '0.75rem' }}
        >
          {successMessage}
        </div>
      )}
      <table className="reviews-table">
        <thead>
          <tr>
            <th>Reviewer Name</th>
            <th>Review</th>
            <th>Rating</th>
            <th>Date Submitted</th>
            {canAccessActions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {reviewList.length > 0 ? (
            reviewList.map(review => (
              <tr key={review.reviewId}>
                <td>{review.reviewerName}</td>
                <td>{review.review}</td>
                <td>{review.rating}/5</td>
                <td>{new Date(review.dateSubmitted).toLocaleDateString()}</td>
                {canAccessActions && (
                  <td>
                    {isOwner && (
                      <button
                        className="btn btn-warning"
                        onClick={() =>
                          navigate(`/updateReview/${review.reviewId}/edit`)
                        }
                        title="Edit"
                      >
                        Edit
                      </button>
                    )}
                    {(isOwner || isAdmin) && (
                      <BasicModal
                        title="Delete review"
                        confirmText="Delete"
                        onConfirm={async () =>
                          await handleDelete(review.reviewId)
                        }
                        showButton={
                          <button className="btn btn-danger" title="Delete">
                            Delete
                          </button>
                        }
                      >
                        <p>Are you sure you want to delete this review?</p>
                      </BasicModal>
                    )}
                  </td>
                )}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={5} className="text-center">
                No reviews available
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <button
        className="btn btn-warning"
        onClick={() => navigate('/visits')}
        title="Let a review"
      >
        Return to visits
      </button>
    </div>
  );
};

export default ReviewsList;
