import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '@/context/UserContext';
import { getAllReviews } from './Api/getAllReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { deleteReview } from './Api/deleteReview';
import BasicModal from '@/shared/components/BasicModal';
import '../VisitListTable.css';

const CustomerReviewsList: React.FC = (): JSX.Element => {
  const [reviewList, setReviewList] = useState<ReviewResponseDTO[]>([]);
  const navigate = useNavigate();
  const { user } = useUser();
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [showSuccessMessage, setShowSuccessMessage] = useState<boolean>(false);

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
    // No reviewerName fallback — ownership is determined by ownerId from the backend
  }, [user.userId]);

  const handleDelete = async (reviewId: number): Promise<void> => {
    try {
      await deleteReview(reviewId.toString());
      setReviewList(prev =>
        prev.filter(review => review.reviewId !== reviewId)
      );

      // show a short success message (page-level)
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
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {reviewList.length > 0 ? (
            reviewList.map(review => {
              const isOwner = user && review.ownerId === user.userId;

              return (
                <tr key={review.reviewId}>
                  <td>{review.reviewerName}</td>
                  <td>{review.review}</td>
                  <td>{review.rating}/5</td>
                  <td>{new Date(review.dateSubmitted).toLocaleDateString()}</td>
                  <td>
                    {isOwner ? (
                      <>
                        <button
                          className="btn btn-warning"
                          onClick={() => navigate(AppRoutePaths.UpdateReview)}
                          title="Edit"
                        >
                          Edit
                        </button>
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
                          <p>Are you sure you want to delete your review?</p>
                        </BasicModal>
                      </>
                    ) : null}
                  </td>
                </tr>
              );
            })
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
        onClick={() => navigate(AppRoutePaths.CustomerVisits)}
        title="Let a review"
      >
        Return to visits
      </button>
      <button
        className="btn btn-primary"
        onClick={() => navigate(AppRoutePaths.CustomerAddReview)}
        title="Write Review"
      >
        Add Review
      </button>
    </div>
  );
};

export default CustomerReviewsList;
