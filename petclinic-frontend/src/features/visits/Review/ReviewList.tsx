import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllReviews } from './Api/getAllReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import { deleteReview } from './Api/deleteReview';
import { IsOwner, IsAdmin } from '@/context/UserContext';
import BasicModal from '@/shared/components/BasicModal';
import StarRating from '@/features/products/components/StarRating';
import EditingReview from './reviewComponents/EditingReview';
import SvgIcon from '@/shared/components/SvgIcon';

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
    <div className="page-container">
      <div className="visit-table-section">
        <h1>Reviews</h1>
        <button
          className="btn btn-warning"
          onClick={() => navigate('/visits')}
          title="Return to visits"
        >
          Return to visits
        </button>
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
              {canAccessActions && <th></th>}
            </tr>
          </thead>
          <tbody>
            {reviewList.length > 0 ? (
              reviewList.map(review => (
                <tr key={review.reviewId}>
                  <td>{review.reviewerName}</td>
                  <td>{review.review}</td>
                  <td>
                    <StarRating currentRating={review.rating} viewOnly={true} />
                  </td>
                  <td>{new Date(review.dateSubmitted).toLocaleDateString()}</td>
                  {canAccessActions && (
                    <td>
                      {isOwner && (
                        <EditingReview
                          reviewId={review.reviewId.toString()}
                          reviewData={review}
                        />
                      )}
                      {(isOwner || isAdmin) && (
                        <BasicModal
                          title="Delete review"
                          confirmText="Delete"
                          onConfirm={async () =>
                            await handleDelete(review.reviewId)
                          }
                          showButton={
                            <a title="Delete">
                              <SvgIcon id="trash" className="icon-visits" />
                            </a>
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
      </div>
    </div>
  );
};

export default ReviewsList;
