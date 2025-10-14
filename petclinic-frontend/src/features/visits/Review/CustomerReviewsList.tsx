import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '@/context/UserContext';
import { getAllReviews } from './Api/getAllReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { deleteReview } from './Api/deleteReview';

const CustomerReviewsList: React.FC = (): JSX.Element => {
  const [reviewList, setReviewList] = useState<ReviewResponseDTO[]>([]);
  const navigate = useNavigate();
  const { user } = useUser();

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
  }, [user.userId]);

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
      alert('Review deleted successfully!');
    } catch (error) {
      console.error('Error deleting review:', error);
      alert('Error deleting review.');
    }
  };

  return (
    <div className="reviews-container">
      <h1>Reviews</h1>
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
            reviewList.map(review => (
              <tr key={review.reviewId}>
                <td>{review.reviewerName}</td>
                <td>{review.review}</td>
                <td>{review.rating}/5</td>
                <td>{new Date(review.dateSubmitted).toLocaleDateString()}</td>
                <td>
                  <button
                    className="btn btn-warning"
                    onClick={() => navigate(AppRoutePaths.UpdateReview)}
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
        onClick={() => navigate(AppRoutePaths.CustomerVisits)}
        title="Let a review"
      >
        Return to visits
      </button>
    </div>
  );
};

export default CustomerReviewsList;
