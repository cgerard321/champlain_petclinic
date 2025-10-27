import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllReviews } from './Api/getAllReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import { deleteReview } from './Api/deleteReview';
import { IsOwner } from '@/context/UserContext';

const ReviewsList: React.FC = (): JSX.Element => {
  const [reviewList, setReviewList] = useState<ReviewResponseDTO[]>([]);
  const navigate = useNavigate();
  const canAccessActions = IsOwner();

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
    const confirmDelete = window.confirm(
      `Are you sure you want to delete review with ID: ${reviewId}?`
    );
    if (!confirmDelete) return;
    try {
      await deleteReview(reviewId.toString());

      setReviewList(prev =>
        prev.filter(review => review.reviewId !== reviewId)
      );
    } catch (error) {
      console.error('Error deleting review:', error);
      alert('Error deleting review.');
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
                      <button
                        className="btn btn-warning"
                        onClick={() =>
                          navigate(`/updateReview/${review.reviewId}/edit`)
                        }
                        title="Edit"
                      >
                        Edit
                      </button>
                      <button
                        className="btn btn-danger"
                        onClick={() => handleDelete(review.reviewId)}
                        title="Delete"
                      >
                        Delete
                      </button>
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
